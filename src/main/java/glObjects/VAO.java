package glObjects;

import static org.lwjgl.opengl.GL46.*;


public class VAO {
    private int id;

    public VAO() {
        id = glGenVertexArrays();
        System.out.println("VAO " + id + " created");
    }

    public void LinkAttrib(VBO vbo, int layout, int offset, int pointer) {
        vbo.bind();
        glVertexAttribPointer(layout, 4, GL_FLOAT, false, offset, pointer);
        glEnableVertexAttribArray(layout);
        vbo.unbind();
    }

    public void bind() {
        glBindVertexArray(id);
    }

    public void unbind() {
        glBindVertexArray(0);
    }

    public void delete() {
        glDeleteVertexArrays(id);
        System.out.println("VAO " + id + " deleted");
    }
}
