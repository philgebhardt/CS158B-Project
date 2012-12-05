import javax.swing.JLabel;


public class TreeNode {
	private JLabel textLabel;
	private String oid;
	
	public TreeNode (String label, String id)
	{
		textLabel = new JLabel(label);
		oid = id;
	}
	
	public JLabel getLabel()
	{
		return textLabel;
	}
	public String getOID()
	{
		return oid;
	}
}
