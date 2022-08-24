package glObjects;

import java.util.concurrent.Callable;

import static org.lwjgl.opengl.GL46.*;

/**
 * Element Buffer Object, буфер для индексов
 */
public class EBO {
    private final int id;
    private Callable<int[]> callable;

    public EBO(Callable<int[]> callable) {
        id = glGenBuffers();
        this.callable = callable;
        System.out.println("EBO " + id + " created");
    }

    public void bindRefresh() {
        bind();
        refresh();
    }

    public void changeIV(Callable<int[]> callable){
        this.callable = callable;
    }

    public void refresh() {
        int[] verticesData = {};
        try {
            verticesData = callable.call();
        } catch (Exception e) {
            System.out.println("Exception in EBO while transfer vertex data");
            e.printStackTrace();
        }
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, verticesData, GL_DYNAMIC_DRAW);
    }

    public void bind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
    }

    public void unbind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void delete() {
        glDeleteBuffers(id);
        System.out.println("EBO " + id + " deleted");
    }
}
