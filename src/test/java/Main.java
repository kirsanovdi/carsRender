import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;

public class Main {

    private class Skeleton {
        public List<String> keypoint_labels;
        public List<List<Integer>> joints;
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

    private <T> T parseJSON(String path, Class<T> T) {
        return new Gson().fromJson(getStrFromFile(path), T);
    }

    private class Point{
        List<Point> nearby;
        boolean checked;
        int id;
        Point(int id){
            this.id = id;
            nearby = new ArrayList<>();
            checked = false;
        }
    }

    public List<Point> overlap(Point a, Point b){
        List<Point> A = a.nearby, B = b.nearby;
        List<Point> result = new ArrayList<>();
        for (Point p: A) {
            if (B.contains(p)){
                result.add(p);
            }
        }
        return result;
    }

    @Test
    public void main() {
        Skeleton skeleton = parseJSON("C:\\Users\\dimak\\IdeaProjects\\carsRender\\src\\main\\resources\\other\\skeleton.json", Skeleton.class);
        Point[] points = new Point[32];
        for (int i = 0; i < points.length; i++) points[i] = new Point(i);

        for (int i = 0; i < skeleton.joints.size(); i++){
            int a = skeleton.joints.get(i).get(0);
            int b = skeleton.joints.get(i).get(1);
            points[a].nearby.add(points[b]);
            points[b].nearby.add(points[a]);
        }
        Set<String> set = new HashSet<>();

        for(int i = 0; i < points.length; i++){
            Point cur = points[i];
            for (Point near: cur.nearby) {
                List<Point> ovr = overlap(cur, near);
                for (Point o: ovr) {
                    int a = cur.id, b = near.id, c = o.id;
                    int max = Math.max(Math.max(a, b), c);
                    int min = Math.min(Math.min(a, b), c);
                    int mid = a + b + c - max - min;
                    String s = "[" + min + ", " + mid + ", " + max + "], ";
                    set.add(s);
                }
            }
        }
        for (String s: set) {
            System.out.println(s);
        }
    }

}

