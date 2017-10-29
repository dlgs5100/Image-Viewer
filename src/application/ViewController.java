package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.scene.control.Slider;

public class ViewController implements Initializable{
	@FXML
	private AnchorPane AnchorTable;
	@FXML
	private Slider Slider;
	@FXML
	private TextField Text;
	@FXML
	private Button Select;
	@FXML
	private Button Skin;
	@FXML
	private ImageView Image;
	@FXML
	private Label label;
	
	private boolean sensor = false;
	private byte[] buffer = new byte[500*500*4];
	private Main main;
	private File file;
	private Canvas canvas;
	private WritableImage image;
	private PixelReader PR;
	private PixelWriter PW;
	private WritableImage bufImg;
	private GraphicsContext gc;
	
	public void setMain(Main main){
		this.main = main;
	}
	
	@FXML
	private void OnClickSelect(ActionEvent event){
		FileChooser fchooser = new FileChooser();
		FileChooser.ExtensionFilter extFilterpng = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
		fchooser.getExtensionFilters().add(extFilterpng);
		
		file = fchooser.showOpenDialog(null);
		
		try{
			BufferedImage bufferedImage = ImageIO.read(file);
			image = SwingFXUtils.toFXImage(bufferedImage, null);
			//Image.setImage(image);
		}catch(IOException ex){
			Logger.getLogger(ViewController.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		sensor = true;
		
		canvas = new Canvas(800,500);
		
		gc = canvas.getGraphicsContext2D();
		gc.drawImage(image,0,0,500,500);
		AnchorTable.getChildren().add(0, canvas);
		
		//byte[] buffer = new byte[500*500*4];
		
		bufImg = canvas.snapshot(null, null);			//?
		PR = bufImg.getPixelReader();
		PR.getPixels(0, 0, 500, 500, (WritablePixelFormat<ByteBuffer>)PixelFormat.getByteBgraInstance(), buffer, 0, 500*4);
		
		
		int p = 0;
        for (int i = 0; i<500; i++) {
            for (int j=0; j<500; j++) {
                int b = buffer[p]&0xff;
                int g = buffer[p+1]&0xff;
                int r = buffer[p+2]&0xff;
                buffer[p] = (byte) (b&0xff);
                buffer[p+1] = (byte) (g&0xff);
                buffer[p+2] = (byte) (r&0xff);
                p += 4;
            }
        }
	}
	@FXML
	private void OnClickSkin(ActionEvent event){
		if(sensor == true){
		gc = canvas.getGraphicsContext2D();
		PixelReader pr = bufImg.getPixelReader();
		WritableImage outImg = new WritableImage(500, 500); 
        PixelWriter pw = outImg.getPixelWriter();
        for (int y=0; y<500; y++) {
            for (int x=0; x<500; x++) {
                Color c = pr.getColor(x, y);
                if (c.getRed()>c.getGreen()&&c.getGreen()>c.getBlue())
                    pw.setColor(x, y, c);
                else
                    pw.setColor(x, y, Color.BLACK);
            }
        }
        gc.drawImage(outImg, 0, 0, 500, 500);
	}
	}

	private double value = 0, slide_value;
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		Slider.setValue(value);
		Slider.maxProperty().set(255);
		Slider.minProperty().set(-255);
		Text.textProperty().bindBidirectional(Slider.valueProperty(), NumberFormat.getNumberInstance().getIntegerInstance());
		
		Slider.valueProperty().addListener(e->setContrastBySlider());
	}
	public void setContrastBySlider(){
		if(sensor == true){
		byte[] pixel = new byte[500*500*4];
		pixel = buffer.clone();
		
		slide_value = Slider.getValue();
		
		double f, new_b, new_g, new_r;
		f = (259*(slide_value+255))/(255*(259-slide_value));
		
		int p=0;
		for (int i = 0; i<500; i++) {
            for (int j = 0; j<500; j++) {
            	 new_b = (f*((buffer[p]&0xff)-128)+128);
            	 if(new_b > 255)
            		 new_b = 255;
            	 else if(new_b < 0)
            		 new_b = 0;
            	 
            	 new_g = (f*((buffer[p+1]&0xff)-128)+128);
            	 if(new_g > 255)
            		 new_g = 255;
            	 else if(new_g < 0)
            		 new_g = 0;
            	 
            	 new_r = (f*((buffer[p+2]&0xff)-128)+128);
            	 if(new_r > 255)
            		 new_r = 255;
            	 else if(new_r < 0)
            		 new_r = 0;
            	 
            	 pixel[p] = (byte) ((byte) new_b&0xff);
                 pixel[p+1] = (byte) ((byte) new_g&0xff);
                 pixel[p+2] = (byte) ((byte) new_r&0xff);
                 p += 4;
            }
        }
		
		PW = gc.getPixelWriter();
		PixelFormat<ByteBuffer> pf = PixelFormat.getByteBgraInstance();
		PW.setPixels(0, 0, 500, 500, pf, pixel, 0, 500*4);
		}
	}
}
