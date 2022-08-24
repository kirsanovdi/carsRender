import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Q;

public class CLint {
    private final Engine engine;
    private final Thread command;

    private final Map<Instruction, Boolean> instructions;

    public CLint(Engine engine) {
        this.engine = engine;
        this.command = new Thread(this::processCommand);
        instructions = new HashMap<>();
        for (Instruction i : Instruction.values()) instructions.put(i, true);
    }

    public void launch() {
        command.start();
        System.out.println("CLint launched");
    }

    private void keyHandler(long window, Instruction i, int key, Runnable runnable) {
        if (instructions.get(i) && glfwGetKey(window, key) == GLFW_PRESS) {
            runnable.run();
            instructions.put(i, false);
        }
        if (!instructions.get(i) && glfwGetKey(window, key) == GLFW_RELEASE) {
            instructions.put(i, true);
        }
    }

    public void handleInput(long window) {
        keyHandler(window, Instruction.STOP, GLFW_KEY_Q, engine::stop);
        keyHandler(window, Instruction.FULL, GLFW_KEY_R, () -> engine.switchRenderType(Instruction.FULL));
        keyHandler(window, Instruction.SKELETON, GLFW_KEY_T, () -> engine.switchRenderType(Instruction.SKELETON));
        keyHandler(window, Instruction.JOINTS_ONLY, GLFW_KEY_Y, () -> engine.switchRenderType(Instruction.JOINTS_ONLY));
        keyHandler(window, Instruction.VISIBLE_ONLY, GLFW_KEY_U, () -> engine.switchRenderType(Instruction.VISIBLE_ONLY));
    }

    private void handleCommand(String command) {
        switch (command) {
            case "list" -> engine.printModelList();
        }
    }

    private void processCommand() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (!engine.stop) {
            try {
                if (br.ready()) {
                    String s = br.readLine();
                    handleCommand(s);
                }
                Thread.sleep(100L);
            } catch (Exception e) {
                System.out.println("exception in reading console command");
            }

        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("CLint stopped");
    }
}
