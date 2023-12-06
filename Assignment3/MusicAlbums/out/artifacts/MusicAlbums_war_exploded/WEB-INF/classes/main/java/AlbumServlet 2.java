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

        res.setContentType("application/json");
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("{ \"albumID\": \"123\", \"title\": \"Example Title\", \"year\": \"Example Year\" }");
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
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("albumID", "123");  // This is a fixed ID as per your previous info
            jsonResponse.addProperty("imageSize", 10);

            // Sending the JSON response back to the client
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();
            out.println(jsonResponse.toString());

        } catch (FileUploadException e) {
            throw new ServletException("Cannot parse multipart request.", e);
        }
    }

    private boolean isUrlValid(String[] urlPath) {
        // TODO: validate the request url path according to the API spec
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        return true;
    }
}
