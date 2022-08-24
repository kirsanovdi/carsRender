package glObjects;


import java.util.concurrent.Callable;
import static org.lwjgl.opengl.GL46.*;


public class VBO {
    private final int id;
    private Callable<float[]> callable;

    public VBO(Callable<float[]> callable) {
        id = glGenBuffers();
        this.callable = callable;
        System.out.println("VBO " + id + " created");
    }

    public void bindRefresh() {
        bind();
        refresh();
    }

    public void changeIV(Callable<float[]> callable){
        this.callable = callable;
    }

    public void refresh() {
        float[] verticesData = {};
        try {
            verticesData = callable.call();
        } catch (Exception e) {
            System.out.println("Exception in VBO while transfer vertex data");
            e.printStackTrace();
        }
        glBufferData(GL_ARRAY_BUFFER, verticesData , GL_DYNAMIC_DRAW);
    }

    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, id);
    }

    public void unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void delete() {
        glDeleteBuffers(id);
        System.out.println("VBO " + id + " deleted");
    }
}
