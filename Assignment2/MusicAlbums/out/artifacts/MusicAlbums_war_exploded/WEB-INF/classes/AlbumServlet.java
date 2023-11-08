import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.*;
import java.util.List;


@WebServlet(name = "AlbumServlet", value = "/albums")
public class AlbumServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        String pathInfo = req.getPathInfo();
        String[] parts = pathInfo.split("/");

        String albumId = null;
        if (parts.length > 1) {
            albumId = parts[1];
        }
        if (albumId == null) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("missing paramterers");
            return;
        }

        // write to database
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM albums WHERE id = ?";
            System.out.println("DEBUG:: connected to database, start to query.");
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                System.out.println("DEBUG:: query prepared statement.");
                statement.setInt(1, Integer.valueOf(albumId));
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // Convert the result into JSON
                        JsonObject jsonResponse = new JsonObject();
                        jsonResponse.addProperty("albumId", resultSet.getString("id"));
                        jsonResponse.addProperty("title", resultSet.getString("title"));
                        jsonResponse.addProperty("year", resultSet.getString("year"));
                        // TODO: add other album properties as needed

                        res.setContentType("application/json");
                        res.setStatus(HttpServletResponse.SC_OK);
                        res.getWriter().write(jsonResponse.toString());
                    } else {
                        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        res.getWriter().write("Album not found");
                    }
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Database access error", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check that we have a file upload request
        if (!ServletFileUpload.isMultipartContent(request)) {
            // Not a multipart/form-data request
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Error: not a multipart request");
            return;
        }

        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        try {
            List<FileItem> formItems = upload.parseRequest(request);

            String image = null;
            String profileJson = null;

            for (FileItem item : formItems) {
                if (item.isFormField()) {
                    // Process regular form field (input type="text|radio|checkbox|etc", select, etc).
                    String fieldName = item.getFieldName();
                    String fieldValue = item.getString();

                    if ("profile".equals(fieldName)) {
                        profileJson = fieldValue;
                    }
                } else {
                    // Process form file field (input type="file").
                    String fieldName = item.getFieldName();
                    if ("image".equals(fieldName)) {
                        image = item.getString();
                    }
                }
            }
            // After processing the form items and before sending the response
            try (Connection connection = getConnection()) {
                JsonObject profile = JsonParser.parseString(profileJson).getAsJsonObject();
                byte[] imageBytes = image.getBytes();
                String artist = profile.get("artist").getAsString();
                String title = profile.get("title").getAsString();
                int year = profile.get("year").getAsInt();

                String insertSql = "INSERT INTO albums (image, artist, title, year) VALUES (?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                    statement.setBytes(1, imageBytes);
                    statement.setString(2, artist);
                    statement.setString(3, title);
                    statement.setInt(4, year);
                    System.out.println("DEBUG:: insert prepared statement:" + statement.toString());
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                throw new ServletException("Database insert error", e);
            }

//            JsonObject jsonResponse = new JsonObject();
//            jsonResponse.addProperty("albumId", "123");  // This is a fixed ID as per your previous info
//            jsonResponse.addProperty("imageSize", 10);
//
//            // Sending the JSON response back to the client
//            response.setContentType("application/json");
//            response.setStatus(HttpServletResponse.SC_OK);
//            PrintWriter out = response.getWriter();
//            out.println(jsonResponse.toString());

        } catch (FileUploadException e) {
            throw new ServletException("Cannot parse multipart request.", e);
        }
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String url = "jdbc:postgresql://database-2.cvcsfgnpc4bt.us-west-2.rds.amazonaws.com:5432/cs6650";
        String username = "postgres";
        String password = "12345678";
        System.out.println("DEBUG:: Start to connect to database");

        return DriverManager.getConnection(url, username, password);
    }

}
