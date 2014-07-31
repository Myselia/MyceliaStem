package cms.controller.command;
import cms.view.AddressBar;

public class CommandCall extends AbstractCommand{
	private final static String command_signature = "call";
	
	@Override
	public void action(String arg){
		String[] parameters = super.commandParam(arg);
		if(parameters.length > 1){
			if(parameters[1].equals("def")){
				define();
			}else{
				try{
					int call = Integer.parseInt(parameters[1]);
					AddressBar.unselectLast(AddressBar.nodeButton(call));
					AddressBar.nodeButton(call).select(true);
				}catch(NumberFormatException e){
					AddressBar.unselectLast(null);
				}catch(Exception e){
					System.out.println("e>No such node ID");
				}
			}	
		}else{
			System.out.println("e>Call Command Incomplete");
		}		
	}

	@Override
	public void define() {
		System.out.println("Parameters: 'def' '#' 'void' ");
		System.out.println("Selects a node in the grid.");
		
	}
	
	@Override
	public String getCommandSignature(){
		return CommandCall.command_signature;
	}

}
