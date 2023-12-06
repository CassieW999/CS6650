import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ReviewServlet", value = "/review")
public class ReviewServlet extends HttpServlet {

    // the pool is used to store channels
    private ObjectPool<Channel> pool;
    private final static String QUEUE_NAME = "ReviewServletPostQueue";

    public void init() {
        this.pool = new GenericObjectPool<Channel>(new ConnectionPoolFactory());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String[] parts = pathInfo.split("/");

        String isLike = null;
        String albumId = null;
        if (parts.length < 3) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("missing paramterers");
            return;
        }
        isLike = parts[1];
        albumId = parts[2];

        try {
            JsonObject reviewMsg = new JsonObject();
            reviewMsg.addProperty("albumId", albumId);
            reviewMsg.addProperty("isLike", isLike);
            Channel channel = null;
            try {
                channel = pool.borrowObject();
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                channel.basicPublish("", QUEUE_NAME, null, reviewMsg.toString().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (channel != null) {
                    try {
                        pool.returnObject(channel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("{ \"error\": \"Internal server error\" }");
        }

        // generate success response
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("application/json");
        res.getWriter().write("success");
    }
}