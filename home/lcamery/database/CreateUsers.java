import java.io.*;

public class CreateUsers{

	public static void main (String[] args){
		String[] users = new String[200];
		String[] passwords = new String[200];
		try{
		StringBuilder sb = new StringBuilder();
		BufferedWriter bw = new BufferedWriter(new FileWriter("theFile.txt"));
		for(int i = 0; i < 200; i++){
			users[i]= Long.toHexString(Double.doubleToLongBits(Math.random())).substring(7);
			for(int x = 0; x < i; x++){
				if(users[x].equals(users[i])){
					System.out.println("ALERT DUPLICATE AT "+x);
				}
			}
			passwords[i] = Long.toHexString(Double.doubleToLongBits(Math.random())) + Long.toHexString(Double.doubleToLongBits(Math.random()));
			sb.append(users[i] + ","+ passwords[i] + ";");
		}
		bw.write(sb.toString());
		bw.close();
		}catch(IOException e){System.out.println("Exception!");}
		System.out.println("Complete!");
	}
}
