package engine;

import controller.Instruction;
import graphics.Camera;
import graphics.Display;
import org.joml.Vector3f;

import java.io.*;
import java.util.*;


public class Engine {
    public final Display display;
    private final Thread engine;

    private final String[] modelsList;
    private final List<Model> models;

    public boolean stop;

    public Engine(Display display) {
        this.display = display;

        this.engine = new Thread(this::processEngine);
        this.models = new ArrayList<>();

        final File folder = new File("src/main/resources/models");
        final File[] files = folder.listFiles();

        modelsList = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            modelsList[i] = files[i].getAbsolutePath();
        }

        //no 4

        models.add(new Model(new Vector3f(0f, 0f, -6f), modelsList[0], "skeleton.json", this));
        models.add(new Model(new Vector3f(2.5f, 0f, -6f), modelsList[1], "skeleton.json", this));
        models.add(new Model(new Vector3f(5f, 0f, -6f), modelsList[2], "skeleton.json", this));

        //models.add(new engine.Model(new Vector3f(0f, -1f, 0f), "src\\main\\resources\\other\\pile3d.json", "pile.json", this));

        models.add(new Model(new Vector3f(0f, 0f, -12f), modelsList[3], "skeleton.json", this));
        models.add(new Model(new Vector3f(2.5f, 0f, -12f), modelsList[5], "skeleton.json", this));
        models.add(new Model(new Vector3f(5f, 0f, -12f), modelsList[6], "skeleton.json", this));

        models.add(new Model(new Vector3f(0f, 0f, -18f), modelsList[7], "skeleton.json", this));
        models.add(new Model(new Vector3f(2.5f, 0f, -18f), modelsList[8], "skeleton.json", this));
        models.add(new Model(new Vector3f(5f, 0f, -18f), modelsList[9], "skeleton.json", this));

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 12; j++) {
                models.add(new Model(new Vector3f(2f * (float) i - 2f, -1f, -2f * (float) j), "src\\main\\resources\\other\\pile3d.json", "pile.json", this));
            }
        }

    }

    public List<Model> getModels(){
        return models;
    }

    public boolean isOccluded(Vector3f v3f, Camera camera) {
        boolean result = false;
        for (Model m : models) {
            if (m.isOccluded(v3f, camera)) {
                result = true;
                break;
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

    public void printModelList() {
        for (int i = 0; i < modelsList.length; i++) {
            System.out.println(i + ":\t" + modelsList[i]);
        }
    }

    private void calcVisibility(Model m){
        for(int i = 0; i < m.vertVisibility.length; i++){
            final Vector3f pos = m.getVec3inSkeleton(i);
            m.vertVisibility[i] = isOccluded(pos, display.mainCamera);
        }
    }

    private void processEngine() {
        final int k = 4;
        final int mc = models.size();
        final int perThread = mc / k;
        simpleThread[] simpleThreads = new simpleThread[k];
        for (int i = 0; i < k; i++) {
            final int r = i;
            simpleThreads[i] = new simpleThread();
            simpleThreads[i].changeProcess(() -> {
                for (int j = perThread * r; j <= Math.min(perThread * (r + 1), mc - 1); j++) {
                    calcVisibility(models.get(j));
                }
            });
            simpleThreads[i].start();
        }

        while (!stop) {

            //for (engine.Model m : models) {
            //    m.calcVisibility();
            //}

            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < k; i++) {
            simpleThreads[i].stop();
        }
    }

}
