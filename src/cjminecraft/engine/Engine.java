package cjminecraft.engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.Callbacks.*;

import java.nio.file.*;
import java.util.*;

import javax.lang.model.SourceVersion;
import org.lwjgl.Version;

import cjminecraft.engine.managers.LanguageManager;
import cjminecraft.engine.managers.WindowManager;
import cjminecraft.engine.util.ILaunchClass;
import cjminecraft.engine.util.IManager;

/**
 * The main class which will do all of the magic!
 * 
 * When creating a game, be sure to add a <code>main</code> method, then set the
 * {@link #LAUNCH_PATH_FILE} correctly. Be sure then to call
 * {@link #main(String[])}
 * 
 * @author CJMinecraft
 *
 */
public class Engine implements IManager, ILaunchClass {
	
	/**
	 * The directory of the launch fill. Without this, the game will not work
	 */
	public static String LAUNCH_PATH_FILE = "launch.txt";

	private static Engine instance = new Engine();

	private List<IManager> managers = new ArrayList<IManager>();
	private HashMap<String, String> options = new HashMap<String, String>();

	/**
	 * Where all the magic happens
	 * 
	 * @param args
	 *            The launch arguments
	 * @throws Exception
	 *             Allow any exception to be thrown
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("LWJGL Version: " + Version.getVersion());
		for (String line : Files.readAllLines(Paths.get(LAUNCH_PATH_FILE))) {
			if (!line.startsWith("##") && !line.isEmpty()) {
				if (SourceVersion.isName(line) && !SourceVersion.isKeyword(line)) {
					if (ILaunchClass.class.isAssignableFrom(Class.forName(line))) {
						System.out.println("Found launch class: " + line);
						ILaunchClass launch = (ILaunchClass) Class.forName(line).newInstance();
						launch.addManagers();
					}
				} else {
					String[] details = line.split(":");
					if (details.length < 2)
						continue;
					instance.options.put(details[0], line.substring(details[0].length() + 1));
					System.out.println(String.format("Loading option: %s = %s", details[0],
							line.substring(details[0].length() + 1)));
				}
			}
		}
		instance.preInit();
		instance.init();
		instance.postInit();
		instance.loop();
		instance.cleanUp();
	}

	@Override
	public void addManagers() {
		System.out.println("Adding Managers");
		addManager(LanguageManager.getInstance());
		addManager(WindowManager.getInstance());
	}

	@Override
	public void preInit() throws Exception {
		for (IManager manager : managers) {
			System.out.println(manager.getClass().getSimpleName() + " pre init");
			manager.preInit();
		}
	}

	@Override
	public void init() throws Exception {
		for (IManager manager : managers) {
			System.out.println(manager.getClass().getSimpleName() + " init");
			manager.init();
		}
	}

	@Override
	public void postInit() throws Exception {
		for (IManager manager : managers) {
			System.out.println(manager.getClass().getSimpleName() + " post init");
			manager.postInit();
		}
	}

	@Override
	public void loop() throws Exception {
		while (!glfwWindowShouldClose(WindowManager.getInstance().getWindowId())) {
			for (IManager manager : managers) {
				manager.loop();
			}

			glfwSwapBuffers(WindowManager.getInstance().getWindowId());
			glfwPollEvents();
		}
	}

	@Override
	public void cleanUp() throws Exception {
		for (IManager manager : this.managers) {
			System.out.println(manager.getClass().getSimpleName() + " clean up");
			manager.cleanUp();
		}
		glfwFreeCallbacks(WindowManager.getInstance().getWindowId());
		glfwDestroyWindow(WindowManager.getInstance().getWindowId());
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	/**
	 * Add a manager to be handled automatically
	 * 
	 * @param manager
	 *            The manager to add
	 * @return The engine instance
	 */
	public static Engine addManager(IManager manager) {
		instance.managers.add(manager);
		return instance;
	}

	/**
	 * Get the value of any launch option
	 * 
	 * @param key
	 *            The name of the variable to get the value of
	 * @return The value of the launch option
	 */
	public String getOption(String key) {
		return this.options.getOrDefault(key, "");
	}

	/**
	 * @return the {@link Engine} instance
	 */
	public static Engine getInstance() {
		return instance;
	}

}