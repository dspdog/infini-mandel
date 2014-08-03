package mandel;


import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.WritableRaster;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


import javax.imageio.ImageIO;
import javax.swing.*;


//import org.json.simple.*;
import org.apfloat.*;

//TODO select BigDecimal vs APFloat library based on speed 

public class mandel extends JFrame
        implements MouseListener, MouseMotionListener, KeyListener, FocusListener
{

    public static void main(String[] args)
    {
        mandel f = new mandel ();
        f.setSize(512,512);
        f.setVisible(true);
        f.init();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new FlowLayout());
    }

    public void windowClosing(WindowEvent e)
    {
        dispose();
        System.exit(0);
    }

    static String readFile(String path)
            throws IOException
    {
        Charset encoding= Charset.defaultCharset();
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

    private static void saveImage(Image src, String fileName, String ext) {
        int w = src.getWidth(null);
        int h = src.getHeight(null);
        int type = BufferedImage.TYPE_INT_RGB;  // other options
        BufferedImage dest = new BufferedImage(w, h, type);
        Graphics2D g2 = dest.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();

        File file = new File(fileName + "." + ext);
        //System.out.println(file.getAbsolutePath());
        try {
            ImageIO.write(dest, ext, file);  // ignore returned boolean
        } catch(IOException e) {
            System.out.println("Write error for " + file.getPath() +
                    ": " + e.getMessage());
        }
    }

    int minPixelDrawn=0;
    int maxPixelDrawn=0;

    public void clearPixelsDrawn(){
        minPixelDrawn=0;
        maxPixelDrawn=screenheight*screenwidth;
        for(int i=0; i<screenheight*screenwidth; i++){
            pixelsdrawn[i]=false;
        }
    }

    public class mainthread extends Thread
    {
        public void run()
        {
            while(!quit)
            {
                if(!done)
                {
                    imgSaved = true && !autoPilot; //always save image if autoPilot
                    imgRequest = autoPilot;

                    clearPixelsDrawn();

                    if(!usebignums)
                        drawmandel(pixels);
                    else
                        drawmandel_bignums(pixels);
                    done = true;
                }
                frame++;
                try
                {
                    repaint();
                    sleep(50L); //TODO sleep speed as param
                }
                catch(InterruptedException interruptedexception) { }
            }
        }
    }

    public void loadCoords(int index){
        System.out.println("loading coords " + index + " -- " + APbmaxx.toString());
        renderParams rp = coordsList.get(index);
        APbmaxx = rp.maxR;
        APbminx = rp.minR;
        APbmaxy = rp.maxI;
        APbminy = rp.minI;
        maxx = rp.maxx;
        minx = rp.minx;
        maxy = rp.maxy;
        miny = rp.miny;
        usebignums = rp.usebignums;
        itermaximum = rp.itermaximum;
        quald=rp.quald;
    }

    public mandel()
    {
        int scale = 512;
        precesion = 32;

        processors = java.lang.Runtime.getRuntime().availableProcessors();
        game = new mainthread();
        quit = false;
        done = false;
        usebignums = true;
        imgSaved = false;
        zoom = 8;

        contcolor = true;
        itermaximum = 128;
        quald = 32;
        qualmax = scale;

        palettenum = 0;
        pdone = 0.0D;
        dx = 0.0D;
        dy = 0.0D;
        frame = 0;
        speed = 0;
        toggleInfo = true;

        APbdx = new Apfloat(0, precesion +2);
        APbdy = new Apfloat(0, precesion +2);
        APmag = new Apfloat(1, precesion +2);

        bdx = new BigDecimal(0).setScale(precesion +2);
        bdy = new BigDecimal(0).setScale(precesion +2);
        mag = new BigDecimal(1).setScale(precesion +2);

        screenwidth = 512;
        screenheight = 512;

        maxx = 2D;
        minx = -2D;
        maxy = 2D;
        miny = -2D;
        APbmaxx = new Apfloat(2, precesion + 3);
        APbminx = new Apfloat(-2, precesion + 3);
        APbmaxy = new Apfloat(2, precesion + 3);
        APbminy = new Apfloat(-2, precesion + 3);
        bmaxx = new BigDecimal(2).setScale(precesion +3);
        bminx = new BigDecimal(-2).setScale(precesion +3);
        bmaxy = new BigDecimal(2).setScale(precesion +3);
        bminy = new BigDecimal(-2).setScale(precesion +3);
/*
        try {
			String config = readFile("params.txt").replace(" ", ""); //goes into auto mode if config file is found
	        String[] params = config.split(";");
	        captureName = params[0];
	        screenwidth = Integer.parseInt(params[1]);
	        qualmax = screenwidth;
	        screenheight = Integer.parseInt(params[2]);
	        contcolor = params[3].matches("true");
            usebignums = !params[4].matches("0");
	        quald = Integer.parseInt(params[5]);

	        if(usebignums){
	        	 precesion = Integer.parseInt(params[4]);
	        }

	        minx = Double.parseDouble(params[6]);
	        maxx = Double.parseDouble(params[7]);
	        miny = Double.parseDouble(params[8]);
	        maxy = Double.parseDouble(params[9]);
	        APbminx = new Apfloat(params[6], precesion + 3);
	        APbmaxx = new Apfloat(params[7], precesion + 3);
	        APbminy = new Apfloat(params[8], precesion + 3);
	        APbmaxy = new Apfloat(params[9], precesion + 3);
	        bminx = new BigDecimal(params[6]).setScale(precesion+3);
	        bmaxx = new BigDecimal(params[7]).setScale(precesion+3);
	        bminy = new BigDecimal(params[8]).setScale(precesion+3);
	        bmaxy = new BigDecimal(params[9]).setScale(precesion+3);

	        itermaximum = Integer.parseInt(params[10]);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
        product = screenwidth * screenheight - 1;
        pixels = new int[screenwidth * screenheight];
        dpixels = new double[screenwidth * screenheight];
        pixels2 = new int[screenwidth * screenheight];
        pixelsdrawn = new boolean[screenwidth * screenheight];
        time3 = 0L;
        palette = new int[10240];
        font1 = new Font("Courier", 1, 12);


        try{
            coordsList = loadFromFile(coordsFileName);
            loadCoords(coordsList.size()-1);
        }catch (Exception e){
            resetCoords();
        }

        System.out.println(coordsList.size() + " coords loaded");



    }

    public void drawpalettes()
    {
        int a = 0;
        for(int b = 0; b < 1024; b++)
        {
            int c = Math.abs(b % 64 - 32) * 8;
            if(c == 256)
                c = 255;
            palette[a * 1024 + b] = 0xff000000 | c << 16 | c << 8 | c;
        }

        a = 1;
        for(int b = 0; b < 1024; b++)
        {
            int c = Math.abs(b % 64 - 32) * 8;
            c = 256 - c;
            if(c == 256)
                c = 255;
            if(c < 0)
                c = 0;
            palette[a * 1024 + b] = 0xff000000 | c << 16 | c << 8 | c;
        }

        a = 2;
        for(int b = 0; b < 1024; b++)
        {
            int c = (b % 2) * 255;
            if(c == 256)
                c = 255;
            palette[a * 1024 + b] = 0xff000000 | c << 16 | c << 8 | c;
        }

        a = 3;
        for(int b = 0; b < 1024; b++)
        {
            int c = Color.HSBtoRGB((float)(b % 256) / 256F, 1.0F, 1.0F);
            palette[a * 1024 + b] = c;
        }

        a = 4;
        for(int b = 0; b < 1024; b++)
        {
            int c = Color.HSBtoRGB((float)(b % 16) / 16F, 1.0F, 1.0F);
            palette[a * 1024 + b] = c;
        }

        a = 5;
        for(int b = 0; b < 1024; b++)
        {
            int c = 1;
            if(b % 7 == 0)
                c = 255;
            palette[a * 1024 + b] = 0xff000000 | c << 16 | c << 8 | c;
        }

    }

    public void init()
    {
        start();
    }

    public void start()
    {
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        drawpalettes();
        setLayout(null);
        zoominfo = new TextArea("");
        zoominfo.setBounds(0, 512, 512, 250);
        zoominfo.setBounds(0, 0, 512, 250);
        zoominfo.setEditable(false);
        zoominfo.setFont(font1);
        add(zoominfo);

        for(int x = 0; x < screenwidth; x++)
        {
            for(int y = 0; y < screenheight; y++){
                pixels[x + y * screenwidth] = 0xff000000;
                dpixels[x + y * screenwidth] = 1.0;
            }
        }

        render = createImage(screenwidth, screenheight);
        rg = render.getGraphics();


        done = false;
        game.start();
    }

    public double log2(double x){
        return Math.log(x)/Math.log(2);
    }

    public Apfloat APlog2(Apfloat x){
        return ApfloatMath.log(x, new Apfloat(2, precesion)); //Math.log(x)/Math.log(2);
    }

    public BigDecimal bdlog2(BigDecimal x){
        return bdlog(2,x);
    }

    public static Image getImageFromArray(int[] pixels, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = (WritableRaster) image.getData();
        raster.setPixels(0,0,width,height,pixels);
        return image;
    }

    public void drawmandel(int array[])
    {
        int iters = 0;

        time1 = System.currentTimeMillis();
        time2 = System.currentTimeMillis() - time1;
        for(sx = 0; sx < screenwidth; sx++)
            for(sy = 0; sy < screenheight; sy++)
            {
                int itermax = itermaximum;
                double r = ((double)sx / (double)screenwidth) * (maxx - minx) + minx;
                double i = ((double)sy / (double)screenheight) * (maxy - miny) + miny;
                double r2 = 0.0D;
                double i2 = 0.0D;
                double distsq = 0.0D;

                int multiplier = 1;

                if(contcolor){multiplier=10;}

                for(iters = 0; iters < itermax; iters++)
                {
                    distsq = r2 * r2 + i2 * i2;
                    if(distsq > 4D*multiplier)
                        break;
                    double r3 = (r2 * r2 + r) - i2 * i2;
                    i2 = 2D * i2 * r2 + i;
                    r2 = r3;
                }
                pixels[sx + sy * screenwidth] = iters;

                if(contcolor){
                    if(iters < itermax){
                        dpixels[sx + sy * screenwidth] =
                                (iters + 1.0
                                        - log2(
                                        log2( Math.sqrt(distsq) )
                                )
                                ) /itermaximum;

                    }else{
                        dpixels[sx + sy * screenwidth] = 1.0;
                    }
                }

                if(System.currentTimeMillis() - time1 - time2 > 1000L)
                {
                    time2 = System.currentTimeMillis() - time1;
                    pdone = ((double)sx / (double)(screenwidth - 1)) * 100D;
                    drawtext();
                    repaint();
                }
            }


        pdone = 100D;
        time2 = System.currentTimeMillis() - time1;
        drawtext();
        try{
            getNextPoint();
        }catch(ArrayIndexOutOfBoundsException e){

        }
    }

    public void getNextPoint(){
        System.out.println("getting next point...");

        //get random point, make sure iterations at that point are not at max

        int randomIndex = (int)(Math.random()*(screenheight*screenwidth));
        int randomX = randomIndex%screenwidth;
        int randomY = randomIndex/screenheight;

        System.out.println("next point " + randomX + ", " + randomY);

        while(pixels[randomIndex] == itermaximum || randomIndex < screenwidth*20){
            randomIndex = (int)(Math.random()*(screenheight*screenwidth));
            System.out.println("new next point " + randomX + ", " + randomY);
        }

        //descend gradient to local minimum

        int iN, iE, iS, iW; //values around current pixel
        double diffN=0, diffE=0, diffS=0, diffW=0; //north east south west iteration gradients
        // boolean nextN, nextE, nextS, nextW; //the next direction to go in (up the gradient)
        boolean badPath = false;

        int stepSize = screenwidth/quald;

        ArrayList<Integer> coolPts = new ArrayList<Integer>();

        while(coolPts.size()<5){
            while(dpixels[randomIndex] < 0.9){

                System.out.println("climbing..." + dpixels[randomIndex]);

                iN = randomIndex-screenwidth*stepSize;
                iE=randomIndex+stepSize;
                iS=randomIndex+screenwidth*stepSize;
                iW=randomIndex-stepSize;

                if(iN>=screenwidth*10){ //check bounds
                    diffN = dpixels[iN] - dpixels[randomIndex];
                }else{badPath=true;}

                if(iE<(screenheight-10)*screenwidth){
                    diffE = dpixels[iE] - dpixels[randomIndex];
                }else{badPath=true;}

                if(iS<(screenheight-10)*screenwidth){
                    diffS = dpixels[iS] - dpixels[randomIndex];
                }else{badPath=true;}

                if(iW>=screenwidth*10){
                    diffW = dpixels[iW] - dpixels[randomIndex];
                }else{badPath=true;}

                dpixels[randomIndex]=0;

                if(diffN>=diffE && diffN>=diffW && diffN>=diffS){ //N max
                    randomIndex = iN;
                }
                if(diffE>=diffN && diffE>=diffW && diffE>=diffS){ //E max
                    randomIndex = iE;
                }
                if(diffS>=diffE && diffS>=diffW && diffS>=diffN){ //S max
                    randomIndex = iS;
                }
                if(diffW>=diffN && diffW>=diffE && diffW>=diffS){ //W max
                    randomIndex = iW;
                }
                if(Math.max(Math.max(diffN, diffS), Math.max(diffW, diffE)) < 0){
                    System.out.println("maxima found");
                    dpixels[randomIndex]=2;
                }

                if(badPath){
                    System.out.println("bad path...");
                }
            }
        }

        System.out.println("done climbing!");
        //zoom in?
    }



    public void drawPixelAPMath(boolean bignums, int array[], int _sx, int _sy){
        int iters = 0;

        if(bignums){
            //TODO choose library based on speed

            if(contcolor){APfour = APfour.multiply(new Apfloat(10));}

            Apfloat r = (new Apfloat((double)_sx / (double)screenwidth, precesion)).multiply(xAPRange).add(APbminx);
            Apfloat i = (new Apfloat((double)_sy / (double)screenheight, precesion)).multiply(yAPRange).add(APbminy);
            Apfloat r2 = new Apfloat(0, precesion);
            Apfloat i2 = new Apfloat(0, precesion);
            Apfloat r3 = new Apfloat(0, precesion);
            Apfloat distsq = new Apfloat(0, precesion);
            int itermax = itermaximum;
            for(iters = 0; iters < itermax; iters++)
            {
                distsq = ApfloatMath.multiplyAdd(r2, r2, i2, i2); //r2.multiply(r2).add(i2.multiply(i2));
                if(APfour.compareTo(distsq) == -1)
                    break;
                r3 = ApfloatMath.multiplySubtract(r2, r2, i2, i2).add(r); //r2.multiply(r2).subtract(i2.multiply(i2)).add(r);
                i2 = APtwo.multiply(i2).multiply(r2).add(i);
                r2 = r3;
            }

            Apfloat APiters = new Apfloat(iters);
            double ApDv = 0.0;

            if(iters < itermax){
                ApDv = (APiters.add(Apfloat.ONE).subtract(APlog2(APlog2( ApfloatMath.sqrt(distsq))))).divide(new Apfloat(itermaximum)).doubleValue();
            }

            int sx2 = _sx;
            int sy2 = _sy;
            int sx2b = sx2;
            int sy2b = sy2;
            for(sx2 = _sx - 1; sx2 < _sx + screenwidth / quald; sx2++)
            {
                sx2b = sx2;
                if(sx2b > screenwidth - 1)
                    sx2b = screenwidth - 1;
                if(sx2b < 0)
                    sx2b = 0;
                for(sy2 = _sy - 1; sy2 < _sy + screenheight / quald; sy2++)
                {
                    sy2b = sy2;
                    if(sy2b > screenheight - 1)
                        sy2b = screenheight - 1;
                    if(sy2b < 0)
                        sy2b = 0;
                    array[sx2b + sy2b * screenwidth] = iters;

                    if(contcolor){

                        if(iters < itermax){
                            dpixels[sx2b + sy2b * screenwidth] = ApDv;
                        }else{
                            dpixels[sx2b + sy2b * screenwidth] = 1.0;
                        }
                    }

                }

            }
        }
        pixelsDone++;
    }

    public static BigDecimal sqrt(BigDecimal A, final int SCALE) {
        BigDecimal x0 = new BigDecimal("0");
        final BigDecimal two = new BigDecimal("2");
        BigDecimal x1 = new BigDecimal(Math.sqrt(A.doubleValue()));
        while (!x0.equals(x1)) {
            x0 = x1;
            x1 = A.divide(x0, SCALE, BigDecimal.ROUND_HALF_UP);
            x1 = x1.add(x0);
            x1 = x1.divide(two, SCALE, BigDecimal.ROUND_HALF_UP);

        }
        return x1;
    }

    public void drawPixelJavaMath(boolean bignums, int array[], int _sx, int _sy){
        int iters = 0;

        if(bignums){
            //TODO choose library based on speed

            if(contcolor){APfour = APfour.multiply(new Apfloat(10));}

            BigDecimal r = (new BigDecimal((double)_sx / (double)screenwidth).setScale(precesion)).multiply(xRange).add(bminx);
            BigDecimal i = (new BigDecimal((double)_sy / (double)screenheight).setScale(precesion)).multiply(yRange).add(bminy);
            BigDecimal r2 = new BigDecimal(0).setScale(precesion);
            BigDecimal i2 = new BigDecimal(0).setScale(precesion);
            BigDecimal r3 = new BigDecimal(0).setScale(precesion);
            BigDecimal distsq = new BigDecimal(0).setScale(precesion);


            int itermax = itermaximum;
            for(iters = 0; iters < itermax; iters++)
            {
                distsq = r2.multiply(r2).add(i2.multiply(i2));
                if(four.compareTo(distsq) == -1)
                    break;
                r3 = r2.multiply(r2).subtract(i2.multiply(i2)).add(r);
                i2 = two.multiply(i2).multiply(r2).add(i);
                r2 = r3;
            }

            BigDecimal bditers = new BigDecimal(iters);
            double Dv = 0.0;

            if(iters < itermax){
                Dv = 0;//(bditers.add(BigDecimal.ONE).subtract(bdlog2(bdlog2(sqrt(distsq, precesion))))).divide(new BigDecimal(itermaximum)).doubleValue();
            }

            int sx2 = _sx;
            int sy2 = _sy;
            int sx2b = sx2;
            int sy2b = sy2;
            for(sx2 = _sx - 1; sx2 < _sx + screenwidth / quald; sx2++)
            {
                sx2b = sx2;
                if(sx2b > screenwidth - 1)
                    sx2b = screenwidth - 1;
                if(sx2b < 0)
                    sx2b = 0;
                for(sy2 = _sy - 1; sy2 < _sy + screenheight / quald; sy2++)
                {
                    sy2b = sy2;
                    if(sy2b > screenheight - 1)
                        sy2b = screenheight - 1;
                    if(sy2b < 0)
                        sy2b = 0;
                    array[sx2b + sy2b * screenwidth] = iters;

                    if(contcolor){

                        if(iters < itermax){
                            dpixels[sx2b + sy2b * screenwidth] = 0;
                        }else{
                            dpixels[sx2b + sy2b * screenwidth] = 1.0;
                        }
                    }

                }

            }
        }
        pixelsDone++;
    }

    class mandelTask implements Runnable {
        int array[];
        int processorNo;
        boolean bignums = false;

        public mandelTask(int _array[], int _processorNo, boolean _bignums){
            array = _array;
            processorNo = _processorNo;
            bignums = _bignums;
        }

        public void run() {
            workForProcessor(array, processorNo, bignums);
            onProcessorDone();
        }
    }


    public void workForProcessor(int array[], int processorNo, boolean bignums){
        //TODO if(bignums)

        int _sx, _sy;

        for(_sx = processorNo*screenwidth/processors; _sx < (processorNo+1)*screenwidth/processors; _sx += screenwidth / quald){
            for(_sy = 0; _sy < screenheight; _sy += screenheight / quald)
            {
                //drawPixelJavaMath(true, array, _sx, _sy);
                drawPixelAPMath(true, array, _sx, _sy);
            }

            if(lastGUIUpdate - System.currentTimeMillis() > 500){
                drawtext();
                repaint();
                lastGUIUpdate = System.currentTimeMillis();
            }
        }
    }

    public void onProcessorDone(){
        processorsDone++;
        System.out.println(processorsDone + "/" + processors + " PROCESSORS DONE");

        time2 = System.currentTimeMillis() - time1;
        drawtext();
        repaint();

        if(processorsDone==processors){
            onAllProcessorsDone();
        }
    }

    public void onAllProcessorsDone(){
        System.out.println("ALL DONE");
        time2 = System.currentTimeMillis() - time1;
        pixelsDone = quald*quald;
        imgRequest = autoPilot; //request img if autopilot

        drawtext();
        repaint();
    }

    public void drawmandel_bignums(int array[])
    {
        pixelsDone = 0;
        processorsDone=0;
        time1 = System.currentTimeMillis();
        time2 = System.currentTimeMillis() - time1;
        APbmaxx = APbmaxx.precision(precesion);// bmaxx.setScale(precesion, 3);
        APbminx = APbminx.precision(precesion);
        APbmaxy = APbmaxy.precision(precesion);
        APbminy = APbminy.precision(precesion);
        bmaxx = bmaxx.setScale(precesion);// bmaxx.setScale(precesion, 3);
        bminx = bminx.setScale(precesion);
        bmaxy = bmaxy.setScale(precesion);
        bminy = bminy.setScale(precesion);

        xAPRange = APbmaxx.subtract(APbminx);
        yAPRange = APbmaxy.subtract(APbminy);

        xRange = bmaxx.subtract(bminx);
        yRange = bmaxy.subtract(bminy);

        APtwo = APtwo.precision(precesion);
        APfour = APfour.precision(precesion);
        two = two.setScale(precesion);
        four = four.setScale(precesion);

        Thread[] threads = new Thread[processors];

        for(int i = 0; i < processors; i++){ //TODO do this right....
            mandelTask mt = new mandelTask(array, i, true);
            threads[i] = new Thread(mt);
            threads[i].start();
            drawtext();
            repaint();
        }

        pdone = 100D;
        time2 = System.currentTimeMillis() - time1;
        drawtext();
    }

    public void drawtext()
    {
        lastGUIUpdate = System.currentTimeMillis();
        String ztext = "";
        ztext = "Draw time: " + String.valueOf(time2) + " ms";

        if(usebignums){
            long totalPixels = quald * quald;
            boolean allDone = pixelsDone==totalPixels;

            if(allDone){
                ztext = ztext.concat(" (DONE)");
            }else{
                pdone = 100.0 * pixelsDone / totalPixels;
                ztext = ztext.concat(" (" + pdone + "% Done)");
            }
        }else{
            if((!done) & (pdone != 100D))
                ztext = ztext.concat(" (" + pdone + "% Done)");
            else
                ztext = ztext.concat(" (DONE)");
        }

        ztext = ztext.concat("\n");
        switch(palettenum)
        {
            case 0: // '\0'
                ztext = ztext.concat("Palette (P): 1/6 Normal");
                break;

            case 1: // '\001'
                ztext = ztext.concat("Palette (P): 2/6 Inverted");
                break;

            case 2: // '\002'
                ztext = ztext.concat("Palette (P): 3/6 Binary");
                break;

            case 3: // '\003'
                ztext = ztext.concat("Palette (P): 4/6 Rainbow");
                break;

            case 4: // '\004'
                ztext = ztext.concat("Palette (P): 5/6 Acid");
                break;

            case 5: // '\005'
                ztext = ztext.concat("Palette (P): 6/6 Phantom");
                break;
        }
        if(palettenum != 2)
            ztext = ztext.concat(" w/ Cycling x" + speed + " (i/o) ");
        else
            ztext = ztext.concat(" (this palette can't cycle)");
        ztext = ztext.concat("\n");
        ztext = ztext.concat(String.valueOf(itermaximum) + " Iterations (9/0)\n");
        ztext = ztext.concat("Zoom Per Click (+/-): " + String.valueOf(zoom / 2) + "\n");
        ztext = ztext.concat("Continuous Coloring (c): " + (contcolor ? "ON" : "OFF") + "\n");

        if(mousex+mousey*screenwidth<screenwidth*screenwidth){
            if(contcolor){
                ztext = ztext.concat("Iterations @ Mouse:\n");
                ztext = ztext.concat("\tEstimate: " + dpixels[mousex+mousey*screenwidth]*itermaximum + " (" + dpixels[mousex+mousey*screenwidth] + ")\n");
                double dp = dpixels[mousex+mousey*screenwidth];
                ztext = ztext.concat("\t    True: " +pixels[mousex+mousey*screenwidth]+ "\n");
            }else{
                ztext = ztext.concat("Iterations @ Mouse: " + pixels[mousex+mousey*screenwidth] + "\n");
            }
        }else{
            ztext = ztext.concat("Iterations @ Mouse: (out of bounds) \n");
        }


        if(usebignums)
        {
            ztext = ztext.concat("Arb. Precision On (Space): ON\n");
            ztext = ztext.concat("\tResolution (;'): " + String.valueOf(quald) + "\n");
            ztext = ztext.concat("\t Precision ([]): " + String.valueOf(precesion) + "\n");
            /*
            ztext = ztext.concat("Min R:   " + bmaxx.toString() + "\n");
            ztext = ztext.concat("Min R:   " + bminx.toString() + "\n");
            ztext = ztext.concat("Max I:   " + bmaxy.toString() + "\n");
            ztext = ztext.concat("Min I:   " + bminy.toString() + "\n");
            ztext = ztext.concat("Mouse R: " + bdx.toString() + "\n");
            ztext = ztext.concat("Mouse I: " + bdy.toString() + "\n");     
            */

            ztext = ztext.concat("Min R:   " + APbmaxx.toString(true) + "\n");
            ztext = ztext.concat("Min R:   " + APbminx.toString(true) + "\n");
            ztext = ztext.concat("Max I:   " + APbmaxy.toString(true) + "\n");
            ztext = ztext.concat("Min I:   " + APbminy.toString(true) + "\n");
            ztext = ztext.concat("Mouse R: " + APbdx.toString(true) + "\n");
            ztext = ztext.concat("Mouse I: " + APbdy.toString(true) + "\n");
        } else
        {
            ztext = ztext.concat("Arb. Precision (Space): OFF\n");
            ztext = ztext.concat("Max R:   " + String.valueOf(maxx) + "\n");
            ztext = ztext.concat("Min R:   " + String.valueOf(minx) + "\n");
            ztext = ztext.concat("Max I:   " + String.valueOf(maxy) + "\n");
            ztext = ztext.concat("Min I:   " + String.valueOf(miny) + "\n");
            ztext = ztext.concat("Mouse R: " + String.valueOf(dx) + "\n");
            ztext = ztext.concat("Mouse I: " + String.valueOf(dy) + "\n");
        }

        ztext = ztext.concat("Mouse (x,y): " + String.valueOf(mousex) + "," + String.valueOf(mousey) + "\n");

        if(APmag.compareTo(new Apfloat(1, precesion)) <= 0){

        }

        long log10=0;
        if(APmag.compareTo(new Apfloat(1)) <= 0)
            ztext = ztext.concat("Magnification:<=" + APmag.floor().toString(true));
        else
            log10 = APmag.scale();

        //if(precesion < APmag.floor().toRadix(10).scale()){
        //	precesion =
        //	System.out.println("NOTICE - Automatically updated precision to " + precesion);
        //}

        ztext = ztext.concat("Magnification: 10^" + log10 + "\n");
        ztext = ztext.concat(processors + " processors");
        ztext = ztext.concat("\n(precesion) to save img");
        ztext = ztext.concat("\n(t) to toggle this window");
        ztext = ztext.replaceAll(" -", "-");
        zoominfo.setVisible(toggleInfo);
        zoominfo.setText(ztext);
    }

    public void update(Graphics gr)
    {
        paint(gr);
    }

    public int interpColor(int shift, double dp, int _palettenum){
        double crossfade = itermaximum*dp - (int)(itermaximum*dp);

        int r1,g1,b1;
        int r2,g2,b2;
        int r3,g3,b3;

        int index = (int)(itermaximum*dp+shift);

        int c1 = palette[_palettenum*1024 + index%1024];//TODO adjustable paletteSize
        int c2 = palette[_palettenum*1024 + (index+1)%1024];

        r1 = new Color(c1).getRed();
        g1 = new Color(c1).getGreen();
        b1 = new Color(c1).getBlue();

        r2 = new Color(c2).getRed();
        g2 = new Color(c2).getGreen();
        b2 = new Color(c2).getBlue();

        r3 = (int)(r1 + crossfade*(r2-r1));
        g3 = (int)(g1 + crossfade*(g2-g1));
        b3 = (int)(b1 + crossfade*(b2-b1));

        //int index = (int)(1.0*dp*itermaximum+shift);
        return  new Color(r3,g3,b3).getRGB();
    }

    public void paint(Graphics gr)
    {
        int x = 0, y = 0;
        int s = speed;
        if(palettenum == 2)
            s = 0;

        if(contcolor){
            if(s >= 0){
                for(x = 0; x < screenwidth * screenheight; x++){
                    pixels2[x] =  interpColor(frame * s, dpixels[x], palettenum);
                }

            }else
                for(x = 0; x < screenwidth * screenheight; x++)
                    pixels2[x] =  interpColor(1024 - frame * -s, dpixels[x], palettenum); //TODO works?

        }else{
            if(s >= 0)
                for(x = 0; x < screenwidth * screenheight; x++)
                    pixels2[x] = palette[palettenum * 1024 + (pixels[x] + frame * s) % 1024];

            else
                for(x = 0; x < screenwidth * screenheight; x++)
                    pixels2[x] = palette[palettenum * 1024 + ((1024 - pixels[x]) + frame * -s) % 1024];
        }

        rg.drawImage(createImage(new MemoryImageSource(screenwidth, screenheight, pixels2, 0, screenwidth)), 0, 0, screenwidth, screenheight, this);

        rg.setColor(Color.blue);
        rg.drawString("Magnification: 10^" + APmag.scale(), 10, screenheight-10);


        gr.drawImage(render, 0, 0, screenwidth, screenheight, this);

        if(!imgSaved && imgRequest){
            String imgName = "_" + coordsList.size();//System.currentTimeMillis() + "";
            System.out.println("saving img" + imgName);
            saveImage(render, imgName, "png");
            imgSaved = true && !autoPilot; //no reset if autopilot
            imgRequest = false;
            coordsList.add(new renderParams(APbminy, APbmaxy, APbminx, APbmaxx, maxx, maxy, minx, miny, quald, usebignums, itermaximum, precesion));
            saveToFile(coordsFileName);
        }


    }

    /*

        ztext = ztext.concat("Max R:   " + APbmaxx.toString(true) + "\n");
        ztext = ztext.concat("Min R:   " + APbminx.toString(true) + "\n");
        ztext = ztext.concat("Max I:   " + APbmaxy.toString(true) + "\n");
        ztext = ztext.concat("Min I:   " + APbminy.toString(true) + "\n");
        ztext = ztext.concat("Mouse R: " + APbdx.toString(true) + "\n");
        ztext = ztext.concat("Mouse I: " + APbdy.toString(true) + "\n");

    * */

    //TODO public int paletteColor(double iters, double shift, int palNo){

    //}

    public void saveToFile(String filename){
        try{
            FileOutputStream fileOut =
                    new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(coordsList);
            out.close();
            fileOut.close();
            System.out.println("saved to "+filename);
        }catch(Exception i){
            i.printStackTrace();
        }
    }

    public ArrayList<renderParams> loadFromFile(String filename) throws IOException, ClassNotFoundException {
        ArrayList<renderParams> loadedList=null;
        FileInputStream fileIn = new FileInputStream(filename);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        loadedList = (ArrayList<renderParams>) in.readObject();
        in.close();
        fileIn.close();
        System.out.println("loaded " + filename + " " + System.getProperty("user.dir"));

        return loadedList;
    }

    public void stop()
    {
        quit = true;
        System.out.println("QUIT");
        //destroy();
        //destroy();
        System.out.println("QUIT2");
    }

    public void mousePressed(MouseEvent arg0)
    {
        double minx2 = minx;
        double miny2 = miny;
        double maxx2 = maxx;
        double maxy2 = maxy;
        time1 = System.currentTimeMillis();
        time2 = System.currentTimeMillis() - time1;
        sx = 0;
        if(arg0.getButton() == 1)
        {
            minx = dx - (maxx2 - minx2) / (double)zoom;
            maxx = dx + (maxx2 - minx2) / (double)zoom;
            miny = dy - (maxy2 - miny2) / (double)zoom;
            maxy = dy + (maxy2 - miny2) / (double)zoom;
        } else
        if(arg0.getButton() == 3)
        {
            minx = dx - ((maxx2 - minx2) * (double)zoom) / 4D;
            maxx = dx + ((maxx2 - minx2) * (double)zoom) / 4D;
            miny = dy - ((maxy2 - miny2) * (double)zoom) / 4D;
            maxy = dy + ((maxy2 - miny2) * (double)zoom) / 4D;
        }

        Apfloat APbminx2 = APbminx.precision(precesion);
        Apfloat APbminy2 = APbminy.precision(precesion);
        Apfloat APbmaxx2 = APbmaxx.precision(precesion);
        Apfloat APbmaxy2 = APbmaxy.precision(precesion);

        Apfloat Apzoom = new Apfloat(zoom, precesion);
        Apfloat Apzoom4 = new Apfloat(zoom/4, precesion);

        if(arg0.getButton() == 1)
        {
            APbminx = APbdx.subtract(APbmaxx2.subtract(APbminx2).divide(Apzoom));// APbminx = APbdx.subtract(APbmaxx2.subtract(APbminx2).divide(new BigDecimal(zoom), 1));
            APbmaxx = APbdx.add(APbmaxx2.subtract(APbminx2).divide(Apzoom));
            APbminy = APbdy.subtract(APbmaxy2.subtract(APbminy2).divide(Apzoom));
            APbmaxy = APbdy.add(APbmaxy2.subtract(APbminy2).divide(Apzoom));
        } else
        if(arg0.getButton() == 3)
        {
            APbminx = APbdx.subtract(APbmaxx2.subtract(APbminx2).multiply(Apzoom4));
            APbmaxx = APbdx.add(APbmaxx2.subtract(APbminx2).multiply(Apzoom4));
            APbminy = APbdy.subtract(APbmaxy2.subtract(APbminy2).multiply(Apzoom4));
            APbmaxy = APbdy.add(APbmaxy2.subtract(APbminy2).multiply(Apzoom4));
        }

        APmag = APmag.precision(precesion +30);//new Apfloat(APmag., precesion + 30);
        if(APbmaxx.subtract(APbminx).compareTo(Apfloat.ZERO)==0)
            APbmaxx = APbmaxx.add(new Apfloat(1, precesion + 30));
        APmag = (new Apfloat(4, precesion +30)).divide(APbmaxx.subtract(APbminx));
        drawtext();
        done = false;
        repaint();
    }

    public void mouseReleased(MouseEvent mouseevent)
    {
    }

    public void mouseEntered(MouseEvent mouseevent)
    {
    }

    public void mouseExited(MouseEvent mouseevent)
    {
    }

    public void mouseDragged(MouseEvent mouseevent)
    {
    }

    public void mouseMoved(MouseEvent arg0)
    {
        time3 = System.currentTimeMillis();
        requestFocus();
        dx = ((double)mousex / (double)screenwidth) * (maxx - minx) + minx;
        dy = ((double)mousey / (double)screenheight) * (maxy - miny) + miny;
        APbdx = new Apfloat((double)mousex / (double)screenwidth, precesion +2);
        APbdy = new Apfloat((double)mousey / (double)screenheight, precesion +2);
        APbdx = APbdx.multiply(APbmaxx.subtract(APbminx));
        APbdy = APbdy.multiply(APbmaxy.subtract(APbminy));
        APbdx = APbdx.add(APbminx);
        APbdy = APbdy.add(APbminy);
        drawtext();
        mousex = arg0.getX();
        mousey = arg0.getY();
    }

    public void keyTyped(KeyEvent keyevent)
    {
    }

    public void resetCoords(){
        System.out.println("resetting coords");
        coordsList = new ArrayList<renderParams>();

        APbmaxx = new Apfloat(2, precesion + 3);
        APbminx = new Apfloat(-2, precesion + 3);
        APbmaxy = new Apfloat(2, precesion + 3);
        APbminy = new Apfloat(-2, precesion + 3);

        coordsList.add(new renderParams(APbminy, APbmaxy, APbminx, APbmaxx, maxx, maxy, minx, miny, quald, usebignums, itermaximum, precesion));
        loadCoords(coordsList.size() - 1);
        saveToFile(coordsFileName);
    }

    public void keyPressed(KeyEvent arg0)
    {
        if(arg0.getKeyChar() == 's')
            imgRequest = true;
        if(arg0.getKeyChar() == 't')
            toggleInfo = !toggleInfo;
        if(arg0.getKeyChar() == ' ')
            usebignums = !usebignums;
        if(arg0.getKeyChar() == 'r')
            done = false;
        if(arg0.getKeyChar() == '\'' && quald < qualmax && usebignums)
            quald*=2;
        if(arg0.getKeyChar() == ';' && quald > 4 && usebignums)
            quald/=2;
        if(arg0.getKeyChar() == '0')
            itermaximum += itermaximum;
        if(!usebignums){done = false;}
        if(arg0.getKeyChar() == '9' && itermaximum > 10)
            itermaximum /= 2;
        if(!usebignums){done = false;}
        if(arg0.getKeyChar() == '[' && precesion > 1)
            precesion--;
        if(arg0.getKeyChar() == ']')
            precesion++;
        if(arg0.getKeyChar() == '=')
            zoom++;
        if(arg0.getKeyChar() == '-' && zoom > 2)
            zoom--;
        if(arg0.getKeyChar() == 'c')
            contcolor = !contcolor;
        if(arg0.getKeyChar() == 'n'){
            resetCoords();
        }

        if(arg0.getKeyChar() == 'p')
        {
            palettenum++;
            if(palettenum > 5)
                palettenum = 0;
            repaint();
        }
        if(arg0.getKeyChar() == 'o')
            speed++;
        if(arg0.getKeyChar() == 'i')
            speed--;
        if(speed > 5)
            speed = 5;
        if(speed < -5)
            speed = -5;
        drawtext();
    }

    public void keyReleased(KeyEvent keyevent)
    {
    }

    public void focusGained(FocusEvent focusevent)
    {
    }

    public void focusLost(FocusEvent focusevent)
    {
    }

    public void mouseClicked(MouseEvent mouseevent)
    {
    }

    public static BigDecimal bdlog(int base_int, BigDecimal x) {
        BigDecimal result = BigDecimal.ZERO;

        BigDecimal input = new BigDecimal(x.toString());
        int decimalPlaces = 100;
        int scale = input.precision() + decimalPlaces;

        int maxite = 10000;
        int ite = 0;
        BigDecimal maxError_BigDecimal = new BigDecimal(BigInteger.ONE,decimalPlaces + 1);
        System.out.println("maxError_BigDecimal " + maxError_BigDecimal);
        System.out.println("scale " + scale);

        RoundingMode a_RoundingMode = RoundingMode.UP;

        BigDecimal two_BigDecimal = new BigDecimal("2");
        BigDecimal base_BigDecimal = new BigDecimal(base_int);

        while (input.compareTo(base_BigDecimal) == 1) {
            result = result.add(BigDecimal.ONE);
            input = input.divide(base_BigDecimal, scale, a_RoundingMode);
        }

        BigDecimal fraction = new BigDecimal("0.5");
        input = input.multiply(input);
        BigDecimal resultplusfraction = result.add(fraction);
        while (((resultplusfraction).compareTo(result) == 1)
                && (input.compareTo(BigDecimal.ONE) == 1)) {
            if (input.compareTo(base_BigDecimal) == 1) {
                input = input.divide(base_BigDecimal, scale, a_RoundingMode);
                result = result.add(fraction);
            }
            input = input.multiply(input);
            fraction = fraction.divide(two_BigDecimal, scale, a_RoundingMode);
            resultplusfraction = result.add(fraction);
            if (fraction.abs().compareTo(maxError_BigDecimal) == -1){
                break;
            }
            if (maxite == ite){
                break;
            }
            ite ++;
        }

        MathContext a_MathContext = new MathContext(((decimalPlaces - 1) + (result.precision() - result.scale())),RoundingMode.HALF_UP);
        BigDecimal roundedResult = result.round(a_MathContext);
        BigDecimal strippedRoundedResult = roundedResult.stripTrailingZeros();
        //return result;
        //return result.round(a_MathContext);
        return strippedRoundedResult;
    }

    boolean autoPilot = true;

    public Image render;
    Graphics rg;
    mainthread game;

    String captureName = "";

    int processors;
    int processorsDone = 0;
    boolean quit;
    boolean done;
    boolean toggleInfo;
    boolean usebignums;
    boolean imgSaved;
    boolean imgRequest;
    int zoom;
    boolean contcolor;
    int itermaximum;
    int quald;
    int qualmax;
    int precesion;
    int palettenum;
    double maxx;
    double maxy;
    double minx;
    double miny;
    double pdone;
    double dx;
    double dy;
    int sx;
    int sy;
    int frame;
    int speed;

    BigDecimal bdx;
    BigDecimal bdy;
    BigDecimal bmaxx;
    BigDecimal bmaxy;
    BigDecimal bminx;
    BigDecimal bminy;
    BigDecimal mag;

    Apfloat APbdx;
    Apfloat APbdy;
    Apfloat APbmaxx;
    Apfloat APbmaxy;
    Apfloat APbminx;
    Apfloat APbminy;
    Apfloat APmag;

    Apfloat APtwo = new Apfloat(2);
    Apfloat APfour = new Apfloat(4);

    BigDecimal two = new BigDecimal(2);
    BigDecimal four = new BigDecimal(4);

    Apfloat xAPRange, yAPRange;
    BigDecimal xRange, yRange;

    int screenwidth;
    int screenheight;
    int product;
    int pixels[];
    double dpixels[];
    boolean pixelsdrawn[];
    int pixels2[];
    int mousex;
    int mousey;
    long time1;
    long time2;
    long time3;

    long lastGUIUpdate = 0;
    long pixelsDone = 0;

    int palette[];
    Font font1;
    TextArea zoominfo;

    static final String coordsFileName = "coords.ser";

    ArrayList<renderParams> coordsList;
}
