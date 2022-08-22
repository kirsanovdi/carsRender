
import glObjects.*;

import java.util.concurrent.Callable;

public class GlController {
    private final VBO vertexBufferObject;
    private final VAO vertexArrayObject;
    private final EBO elementBufferObject;

    public GlController(Callable<int[]> indices, Callable<float[]> vertices){
        vertexBufferObject = new VBO(vertices);
        elementBufferObject = new EBO(indices);
        vertexArrayObject = new VAO();
    }

    public void setupVAO() {
        vertexArrayObject.bind();
    }

    public void update(){
        vertexArrayObject.bind();
        vertexBufferObject.bindRefresh();
        elementBufferObject.bindRefresh();

        vertexArrayObject.LinkAttrib(vertexBufferObject, 0, 20, 0);
        vertexArrayObject.LinkAttrib(vertexBufferObject, 1, 20, 12);

        vertexArrayObject.unbind();
        vertexBufferObject.unbind();
        elementBufferObject.unbind();
    }

    public void destroy() {
        vertexArrayObject.delete();
        vertexBufferObject.delete();
        elementBufferObject.delete();
    }
}
