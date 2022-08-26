package graphics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.opengl.GL46.glDeleteProgram;

public class Shader {

    public final int id;

    public final static Shader shader_standard = new Shader("code/shaders/standard/shader.vert", "code/shaders/standard/shader.frag", "code/shaders/standard/shader.geom");
    public final static Shader shader_joints = new Shader("code/shaders/joints_only/shader.vert", "code/shaders/joints_only/shader.frag", "code/shaders/joints_only/shader.geom");
    public final static Shader shader_frame = new Shader("code/shaders/frame/shader.vert", "code/shaders/frame/shader.frag", "code/shaders/frame/shader.geom");

    public static void clearStatic(){
        shader_standard.delete();
        shader_joints.delete();
    }

    public Shader(String vertexSource, String fragmentSource, String geometrySource) {

        String vertexCode = "", fragmentCode = "", geometryCode = "";
        try {
            vertexCode = Files.readString(new File("src/main/resources/" + vertexSource).toPath());
            fragmentCode = Files.readString(new File("src/main/resources/" + fragmentSource).toPath());
            geometryCode = Files.readString(new File("src/main/resources/" + geometrySource).toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexCode);
        glCompileShader(vertexShader);
        System.out.println(checkCompile(vertexShader));

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentCode);
        glCompileShader(fragmentShader);
        System.out.println(checkCompile(fragmentShader));

        int geometryShader = glCreateShader(GL_GEOMETRY_SHADER);
        glShaderSource(geometryShader, geometryCode);
        glCompileShader(geometryShader);
        System.out.println(checkCompile(geometryShader));

        id = glCreateProgram();
        //System.out.println(id);
        glAttachShader(id, vertexShader);
        glAttachShader(id, fragmentShader);
        glAttachShader(id, geometryShader);
        glLinkProgram(id);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        glDeleteShader(geometryShader);

        System.out.println("graphics.Shader program " + id + " created");
    }

    private String checkCompile(int shaderId) {
        return "shader " + shaderId +
                (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 1 ? " compiled successfully" : " failed to compile");
    }

    public void transferCamera(Camera camera) {
        camera.Matrix(120.0f, 0.1f, 10000.0f, this, "camMatrix");

        //int camPos = glGetUniformLocation(id, "camPos");
        //glUniform3fv(camPos, new float[]{camera.position.x, camera.position.y, camera.position.z});
    }

    public void activate() {
        glUseProgram(id);
    }

    public void delete() {
        glDeleteProgram(id);
        System.out.println("graphics.Shader program " + id + " deleted");
    }
}
