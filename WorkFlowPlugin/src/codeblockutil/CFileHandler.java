package codeblockutil;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

public final class CFileHandler {

	public static String[] readFromFile(JComponent parent, int numLines){
		int numberOfLines = numLines;
		JFileChooser filechooser = new JFileChooser("Import Data From");
		filechooser.addChoosableFileFilter(new CSVFilter());
		int returnVal = filechooser.showOpenDialog(parent.getTopLevelAncestor());
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = filechooser.getSelectedFile();
			List<String> data = new ArrayList<String>();
			BufferedReader input = null;
			String line = null;
			if(file != null){
				try {
					input = new BufferedReader( new FileReader(file) );
					while (( line = input.readLine()) != null && numberOfLines>0){
						numberOfLines--;
						data.add(line);
					}
				}catch (FileNotFoundException ex) {
					ex.printStackTrace();
				}catch (IOException ex){
					ex.printStackTrace();
				}finally{
					try{
						if (input!= null) input.close();
					}catch (IOException ex) {
						ex.printStackTrace();
					}
				}
				return data.toArray(new String[data.size()]);
			}
		}
		return null;
	}
	public static boolean writeToFile(JComponent parent, String source){
		JFileChooser fc = new JFileChooser("Saving Data");
		int returnVal = fc.showSaveDialog(parent.getTopLevelAncestor());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			new CProgressBar("Saving Data to " + f.getName());
			try {
				if(f.getPath().length()>4){
					String extension = f.getPath().substring(f.getPath().length()-4, f.getPath().length());
					if(extension.equals(".csv")){
						System.out.println("Saving with default extension: " + extension);
						//do nothing
					}else{
						System.out.println("Generating extension: " + extension);
						f = new File(f.getPath()+".csv");
					}
				}else{
					System.out.println("Filename too short");
					f = new File(f.getPath()+".csv");
				}
				Writer output = null;
				output = new BufferedWriter( new FileWriter(f) );
				output.write(source);
				output.close();
				return true;
			} catch (IOException io) {
				io.printStackTrace();
				return false;
			}
		}
		return false;
	}
	public static boolean writeToFile(JComponent parent, BufferedImage image){
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(parent.getTopLevelAncestor());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			new CProgressBar("Saving Graph to " + f.getName());
			try {
				if(f.getPath().length()>4){
					String extension = f.getPath().substring(f.getPath().length()-4, f.getPath().length());
					String extension2 = f.getPath().substring(f.getPath().length()-5, f.getPath().length());
					if(extension.equals(".jpg") || extension.equals(".gif") || extension.equals(".tif") || 
							extension.equals(".png") || extension2.equals(".jpeg") || extension2.equals(".tiff")){
						System.out.println("Saving with default extension: " + extension);
						//do nothing
					}else{
						System.out.println("Generating extension: " + extension);
						f = new File(f.getPath()+".jpg");
					}
				}else{
					System.out.println("Filename too short");
					f = new File(f.getPath()+".jpg");
				}
				ImageIO.write(image, "jpg", f);
				return true;
			} catch (IOException io) {
				io.printStackTrace();
				return false;
			}
		}
		return false;
	}
}
