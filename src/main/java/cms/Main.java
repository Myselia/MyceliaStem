package cms;

import cms.communication.Broadcast;
import cms.communication.Server;
import cms.control.CommandSystem;
import cms.control.ConfigHandler;
import cms.databank.OverLord;
import cms.databank.structures.Database;
import cms.display.ProgramWindow;
import cms.monitoring.LogSystem;

public class Main {
	
	public static final int DEFAULT_PORT = 6969; //TODO: change
	public static boolean REROUTE_ERR = false;	//Error Re-Routing to CMS Console
	
	private static Broadcast bcastRunnable = new Broadcast();
	private static Server serverRunnable = new Server(DEFAULT_PORT, 100);
	
	private static Thread bCastCommunicator;
	private static Thread communicator;
	private static Thread data;
	private static Thread display;
	
	public static void main(String[] args) {		
		loadCommands(); //loads the user commands
		ConfigHandler.init();

		//Model
		data = new Thread(new Runnable(){
			public void run() {
				OverLord.build();	
			}
		});
		
		//View
		display = new Thread(new Runnable(){
			public void run() {
				ProgramWindow.initEnvironment();
				LogSystem.log(true, false, "Log System Started");
				System.out.println("Welcome to the CMS v0.6 alpha");
				System.out.println("Enter 'help' for a list of commands");
			}
		});

		communicator = new Thread(serverRunnable);

		try {
			data.start();
			Thread.sleep(2000);
			display.start();
			Thread.sleep(2000);
			communicator.start();
		} catch (InterruptedException e) {
			System.out.println("e>Error starting main threads. Please restart.");
			//e.printStackTrace();
		}
		
		//Add all user defined DBs into the OverLord's database list
		setupDatabases();

	}

	private static void loadCommands(){
		try{
			CommandSystem.setClasses("cms.control.user");
		}catch(Exception e){
			System.out.println("e>" + "cms.control.CommandSystem.setClasses(String) called in");
			System.out.println("e>" + "cms.display.communication.ConsoleDisplay.ConsoleDisplay() threw an exception");
			System.out.println("e>" + "Exception thrown by:" + e.getClass().getCanonicalName());
			System.out.println("Please force kill the application and investigate.");
		}
	}

	public static void startBCastThread(Runnable instance, Thread thread) {
		thread = new Thread(instance);
		setbCastCommunicator(thread);
		thread.start();
	}

	public static Broadcast getBcastRunnable() {
		return bcastRunnable;
	}

	public static Thread getbCastCommunicator() {
		return bCastCommunicator;
	}

	public static void setbCastCommunicator(Thread bCastCommunicator) {
		Main.bCastCommunicator = bCastCommunicator;
	}
	
	private static void setupDatabases() {
		for (int i = 0; i < ConfigHandler.DBCount; i++) {
			//Create DB object with appropriate field values
			Database db = new Database(
					ConfigHandler.configProperties.get("DB_" + i + "_name"), 
					ConfigHandler.configProperties.get("DB_" + i + "_url"), 
					ConfigHandler.configProperties.get("DB_" + i + "_dbname"), 
					ConfigHandler.configProperties.get("DB_" + i + "_user"), 
					ConfigHandler.configProperties.get("DB_" + i + "_password")
					);
			//Add database to central DB repository
			OverLord.dbCore.add(db);
		}
	}
	

}
