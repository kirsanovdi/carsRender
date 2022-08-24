import com.google.gson.Gson;
import org.joml.Intersectionf;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL46.*;

public class Model {
    private volatile int indicesSize, verticesSize;
    public boolean[] vertVisibility;

    private Engine engine;

    private GlController glController;
    private Shader shader;
    private int GL_DRAWMODE;

    private final static Shader shader_standard = new Shader("code/shaders/standard/shader.vert", "code/shaders/standard/shader.frag", "code/shaders/standard/shader.geom");
    private final static Shader shader_joints = new Shader("code/shaders/joints_only/shader.vert", "code/shaders/joints_only/shader.frag", "code/shaders/joints_only/shader.geom");

    //private final GlController controller_standard, controller_skeleton, controller_joints, controller_visible;

    public static void clearStatic(){
        shader_standard.delete();
        shader_joints.delete();
    }

    private final Vector3f position;
    private final Car car;
    private final Skeleton skeleton;

    private class Car {
        public String car_type;
        public String name;
        public List<List<Float>> vertices;
        public List<List<Integer>> faces;
        public Map<String, Integer> pts;
    }

    private class Skeleton {
        public List<String> keypoint_labels;
        public List<List<Integer>> joints;
        public List<List<Integer>> triangles;
    }

    public Model(Vector3f position, String toJSON, String skeletonName, Engine engine) {
        this.engine = engine;
        this.car = parseJSON(toJSON, Car.class);
        this.skeleton = parseJSON("src\\main\\resources\\other\\" + skeletonName, Skeleton.class);

        //this.controller_joints = new GlController(this::getIndices_joints, this::getVertices_joints);
        //this.controller_visible = new GlController(this::getIndices_visible, this::getVertices_visible);
        //this.controller_standard = new GlController(this::getIndices_standard, this::getVertices_standard);
        //this.controller_skeleton = new GlController(this::getIndices_skeleton, this::getVertices_skeleton);

        this.shader = shader_standard;
        this.glController = new GlController(this::getIndices_standard, this::getVertices_standard);
        this.GL_DRAWMODE = GL_TRIANGLES;
        verticesSize = car.vertices.size();
        indicesSize = car.faces.size() * 3;

        this.vertVisibility = new boolean[skeleton.keypoint_labels.size()];
        this.position = new Vector3f(position);
        System.out.println("Model " + this.toString() + "\t" + position + "\t" + toJSON + "\t created");
    }

    public synchronized void switchRenderType(Instruction rt){
        switch (rt){
            case FULL -> {
                shader = shader_standard;
                GL_DRAWMODE = GL_TRIANGLES;
                //glController = controller_standard;
                glController.changeIV(this::getIndices_standard, this::getVertices_standard);
                verticesSize = car.vertices.size();
                indicesSize = car.faces.size() * 3;
            }
            case SKELETON -> {
                shader = shader_standard;
                GL_DRAWMODE = GL_TRIANGLES;
                //glController = controller_skeleton;
                glController.changeIV(this::getIndices_skeleton, this::getVertices_skeleton);
                verticesSize = skeleton.keypoint_labels.size();
                indicesSize = skeleton.triangles.size() * 3;
            }
            case JOINTS_ONLY -> {
                shader = shader_joints;
                GL_DRAWMODE = GL_LINES;
                //glController = controller_joints;
                glController.changeIV(this::getIndices_joints, this::getVertices_joints);
                verticesSize = skeleton.keypoint_labels.size();
                indicesSize = skeleton.joints.size() * 2;
            }
            case VISIBLE_ONLY -> {
                shader = shader_joints;
                GL_DRAWMODE = GL_LINES;
                //glController = controller_visible;
                glController.changeIV(this::getIndices_visible, this::getVertices_visible);
                verticesSize = skeleton.keypoint_labels.size();
                indicesSize = 0;//skeleton.joints.size() * 2;
            }
        }
    }
    public void calcVisibility(){
        for(int i = 0; i < vertVisibility.length; i++){
            final Vector3f v3f = vfl_revY(getTriangleBySkeletonID(i));
            final Vector3f curPos = new Vector3f(v3f.x + position.x, v3f.y + position.y, v3f.z + position.z);

            vertVisibility[i] = engine.isOccluded(curPos, engine.display.mainCamera);
        }
    }
    public synchronized void draw(Camera camera) {
        shader.activate();
        shader.transferCamera(camera);
        glController.update();
        glController.setupVAO();
        glDrawElements(GL_DRAWMODE, indicesSize, GL_UNSIGNED_INT, 0);
    }
    public synchronized void delete() {
        //controller_standard.destroy();
        //controller_joints.destroy();
        //controller_skeleton.destroy();
        //controller_visible.destroy();
        glController.destroy();
        System.out.println("Model " + this.toString() + "\t deleted");
    }

    public void move(float x, float y , float z){
        position.add(x, y, z);
    }
    public void rotate(float aX, float aY, float aZ){

    }

    public synchronized boolean isOccluded(Vector3f v, Camera camera){
        boolean result = false;
        final Vector3f origin = new Vector3f(camera.position.x, camera.position.y, camera.position.z);
        final Vector3f dir = new Vector3f(v).sub(origin);
        for (List<Integer> triangle: skeleton.triangles) {
            if(occlusion(
                    origin,
                    dir,
                    vfl_revY(getTriangleBySkeletonID(triangle.get(0))).add(position),
                    vfl_revY(getTriangleBySkeletonID(triangle.get(1))).add(position),
                    vfl_revY(getTriangleBySkeletonID(triangle.get(2))).add(position)
            )){
                result = true;
                break;
            }
        }
        return result;
    }

    private List<Float> getTriangleBySkeletonID(int i){
        return car.vertices.get(car.pts.get(skeleton.keypoint_labels.get(i)));
    }

    private float[] getVertices_standard() {
        float[] vertices = new float[verticesSize * 5];
        for (int i = 0; i < verticesSize; i++) {
            Vector3f v3f = vfl_revY(car.vertices.get(i));
            vertices[i * 5] = v3f.x + position.x;
            vertices[i * 5 + 1] = v3f.y + position.y;
            vertices[i * 5 + 2] = v3f.z + position.z;
            float height = ((1f - 1f/(1f + v3f.y * v3f.y)) / 2f) * ((1f - 1f/(1f + v3f.z*v3f.z)) / 2f);
            vertices[i * 5 + 3] = height;
            vertices[i * 5 + 4] = (height*100f)%1f;
        }
        return vertices;
    }
    private int[] getIndices_standard() {
        int[] indices = new int[indicesSize];
        for (int i = 0; i < indicesSize; i++) {
            indices[i] = car.faces.get(i / 3).get(i % 3) - 1;
        }
        return indices;
    }

    private float[] getVertices_skeleton(){
        float[] vertices = new float[verticesSize * 5];
        for (int i = 0; i < verticesSize; i++) {
            Vector3f v3f = vfl_revY(getTriangleBySkeletonID(i));
            vertices[i * 5] = v3f.x + position.x;
            vertices[i * 5 + 1] = v3f.y + position.y;
            vertices[i * 5 + 2] = v3f.z + position.z;
            float height = (1f - 1f/(1f + v3f.y * v3f.y)) / 2f;
            vertices[i * 5 + 3] = height;
            vertices[i * 5 + 4] = (height*1000f)%1f;
        }
        return vertices;
    }
    private int[] getIndices_skeleton() {
        int[] indices = new int[indicesSize];
        for (int i = 0; i < indicesSize; i++) {
            indices[i] = skeleton.triangles.get(i / 3).get(i % 3);
        }
        return indices;
    }

    private float[] getVertices_joints(){
        float[] vertices = new float[verticesSize * 5];
        for (int i = 0; i < verticesSize; i++) {
            Vector3f v3f = vfl_revY(getTriangleBySkeletonID(i));
            vertices[i * 5] = v3f.x + position.x;
            vertices[i * 5 + 1] = v3f.y + position.y;
            vertices[i * 5 + 2] = v3f.z + position.z;
            //final Vector3f curPos = new Vector3f(v3f.x + position.x, v3f.y + position.y, v3f.z + position.z);
            final float height = (vertVisibility[i]?1f:0f);
            vertices[i * 5 + 3] = height;
            vertices[i * 5 + 4] = height;
        }
        return vertices;
    }
    private int[] getIndices_joints() {
        int[] indices = new int[indicesSize];
        for (int i = 0; i < indicesSize; i++) {
            indices[i] = skeleton.joints.get(i / 2).get(i % 2);
        }
        return indices;
    }

    private float[] getVertices_visible(){
        float[] vertices = new float[verticesSize * 5];
        for (int i = 0; i < verticesSize; i++) {
            Vector3f v3f = vfl_revY(getTriangleBySkeletonID(i));
            vertices[i * 5] = v3f.x + position.x;
            vertices[i * 5 + 1] = v3f.y + position.y;
            vertices[i * 5 + 2] = v3f.z + position.z;
            //final Vector3f curPos = new Vector3f(v3f.x + position.x, v3f.y + position.y, v3f.z + position.z);
            //final float height = (vertVisibility[i]?1f:0f);
            vertices[i * 5 + 3] = 1f;
            vertices[i * 5 + 4] = 1f;
        }
        return vertices;
    }
    private int[] getIndices_visible() {

        final int[] indices = new int[skeleton.joints.size() * 2];
        int k = 0;
        for (int i = 0; i < skeleton.joints.size(); i++) {
            final int a = skeleton.joints.get(i).get(0);
            final int b = skeleton.joints.get(i).get(1);
            if(!vertVisibility[a] && !vertVisibility[b]){
                indices[k++] = a;
                indices[k++] = b;
            }
        }
        indicesSize = k;
        return Arrays.copyOfRange(indices, 0, k);
    }


    private static boolean occlusion(Vector3f origin, Vector3f dir, Vector3f a, Vector3f b, Vector3f c){
        final float r = 0.1f;
        Vector3f point = new Vector3f(origin).add(dir);
        if (point.distance(a) < r || point.distance(b) < r || point.distance(c) < r) return false;

        float k = Intersectionf.intersectRayTriangle(origin, dir, a, b, c, 0.001f);

        return k >= 0f && k < 1f;
    }
    private static String getStrFromFile(String path) {
        String result = "";
        try {
            result = Files.readString(new File(path).toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    private static  <T> T parseJSON(String path, Class<T> T) {
        return new Gson().fromJson(getStrFromFile(path), T);
    }
    private static Vector3f vfl_revY(List<Float> list) {
        return new Vector3f(list.get(0), -list.get(1), list.get(2));
    }
}
