package org.s449;

import java.io.*;

public class UserEditor {
	// FIXME delete this file
	public static void main(String[] args) throws Exception {
		// Q & E
		UserFile file = new UserFile("users.dat");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("1: Add/Edit User\n2: Delete User\nWhat to do? ");
		int op = Integer.parseInt(br.readLine());
		String name;
		switch (op) {
		case 1:
			System.out.print("Name? ");
			name = br.readLine();
			System.out.print("Real name? ");
			String rName = br.readLine();
			System.out.print("Password? ");
			char[] pass = br.readLine().toCharArray();
			System.out.print("Permissions (15=admin,7=scorer,3=writer,1=reader,0=kiosk)? ");
			int permis = Integer.parseInt(br.readLine());
			System.out.print("Team num? ");
			int team = Integer.parseInt(br.readLine());
			UserData u = new UserData(name, rName, pass, permis, team);
			file.getData().setUserData(name, u);
			file.update();
			break;
		case 2:
			System.out.print("Name? ");
			name = br.readLine();
			file.getData().removeUser(name);
			file.update();
			break;
		default:
		}
		System.exit(0);
	}
}