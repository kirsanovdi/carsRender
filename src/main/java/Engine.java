import org.joml.Vector3f;

import java.io.*;
import java.util.*;


public class Engine {
    public final Display display;
    private final Thread engine;


    private final String[] modelsList;
    public final List<Model> models;

    public boolean stop;

    public Engine(Display display) {
        this.display = display;

        engine = new Thread(this::processEngine);
        models = new ArrayList<>();

        File folder = new File("src/main/resources/models");
        File[] files = folder.listFiles();

        modelsList = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            modelsList[i] = files[i].getAbsolutePath();
        }

        models.add(new Model(new Vector3f(0f, 0f, 0f), modelsList[0], this));
        models.add(new Model(new Vector3f(2.5f, 0f, 0f), modelsList[1], this));
    }

    public boolean isOccluded(Vector3f v3f, Camera camera) {
        boolean result = false;
        synchronized (models) {
            for (Model m : models) {
                if (m.isOccluded(v3f, camera)) {
                    result = true;
                }
            }
        }
        return result;
    }


    public void launch() {
        engine.start();
        System.out.println("engine launched");
    }

    public void stop() {
        stop = true;
        display.stop = true;

        for (Model model : models) {
            model.delete();
        }

        System.out.println("engine stopped");
    }

    public void switchRenderType(Instruction rt) {
        synchronized (models) {
            for (Model m : models) {
                m.switchRenderType(rt);
            }
        }
    }

    /**
     * not on engine
     */
    public void drawModels(Camera camera) {
        synchronized (models) {
            for (Model m : models) {
                m.draw(camera);
            }
        }
    }

    public void printModelList() {
        for (int i = 0; i < modelsList.length; i++) {
            System.out.println(i + ":\t" + modelsList[i]);
        }
    }

    private void processEngine() {
        while (!stop) {
            Model m = models.get(0);
            synchronized (m) {
                //m.move(0f, 0f, 0.02f);
            }
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
