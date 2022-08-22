import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.text.NumberFormat;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Display {
    private long window;
    public final int width;
    public final int height;
    public final String name;
    public boolean stop = false;

    public final Camera mainCamera;
    private Engine engine;
    private CLint cLint;

    private long pTime = 0L;
    private int fCount = 0;

    private void frameRate() {
        long cTime = System.currentTimeMillis();
        if (cTime - pTime >= 1000L) {
           glfwSetWindowTitle(window, getDescription() + "\t " + fCount + "\t " + mainCamera.position.toString(NumberFormat.getIntegerInstance()));
            fCount = 0;
            pTime = cTime;
        }
        fCount++;
    }

    public Display() {
        width = 1600;
        height = 900;
        name = "debug";
        mainCamera = new Camera(width, height, new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, 1f));
        System.out.println(getDescription() + " created");
    }

    private String getDescription() {
        return String.format("Display (%s, %s) \"%s\"", width, height, name);
    }

    private void initialize() {
        // Set up an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(width, height, name, NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");


        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (videoMode == null) throw new RuntimeException("Failed ti get resolution of the primary monitor");

            // Center the window
            glfwSetWindowPos(
                    window,
                    (videoMode.width() - pWidth.get(0)) / 2,
                    (videoMode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
        System.out.println(getDescription() + " initialized");
    }

    public void run() {
        System.out.println(getDescription() + " launched with LWJGL " + Version.getVersion());

        initialize();
        processGUI();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();

        System.out.println(getDescription() + " stopped");
    }

    private void processGUI() {
        GL.createCapabilities();
        glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        //glEnable(GL_CULL_FACE);

        engine = new Engine(this);
        cLint = new CLint(engine);
        engine.launch();
        cLint.launch();

        while (!glfwWindowShouldClose(window) && !stop) {
            glClearColor(0.2f, 0.3f, 0.5f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            mainCamera.mouseInput(window);
            cLint.handleInput(window);

            engine.drawModels(mainCamera);

            frameRate();
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        Model.clearStatic();
    }
}
