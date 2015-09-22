package com.alpine.miner.impls.chorus;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import com.alpine.miner.impls.web.resource.FilePersistence;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Node;


import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.model.impl.OperatorPosition;
import com.alpine.miner.workflow.model.impl.UIOperatorConnectionModel;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.reader.XMLFileReaderParameters;
import com.alpine.miner.workflow.reader.XMLWorkFlowReader;
import org.apache.log4j.Logger;
import com.alpine.utility.xml.XmlDocManager;

/**
 * Given a flow, ChorusUtil has all the functionality necessary to create an png image of the flow
 * and save it to the filesystem.
 * <p/>
 * This is set up only for chorus!
 */
public class ChorusUtil {
    private static Logger itsLogger = Logger.getLogger(ChorusUtil.class);
    private static Map<String, String> imageNameMap = new HashMap<String, String>();

    //default value
    public static int imageSizeX = 800;

    public static int imageSizeY = 600;

    private static int iconSize = 60;

    private static boolean inited = false;

    private static String imageDir = null;

    public static void storeFlowFile(String imgPath, String flowFilePath, String fileId) throws Exception {
        if (!alreadyInitialized()) {
            init(imgPath);
        }
        flowFilePath += FilePersistence.AFM;
        File srcFile = new File(flowFilePath);
        String dPath = createFilePath(fileId, FilePersistence.AFM);
        File destFile = new File(dPath);
        FileUtils.copyFile(srcFile, destFile);

    }

    public static void storeFlowImage(String imgPath, String flowFilePath, String fileId) throws Exception {
        if (!alreadyInitialized()) {
            init(imgPath);
        }

        //read flow
        XMLWorkFlowReader reader = new XMLWorkFlowReader();
        flowFilePath += FilePersistence.AFM;
        OperatorWorkFlow ow = reader.doRead(new XMLFileReaderParameters(
                flowFilePath, System.getProperty("user.name"),
                ResourceType.Personal), Locale.getDefault()
        );
        List<UIOperatorModel> children = ow.getChildList();
        int width = imageSizeX;
        int height = imageSizeY;
        //count size

        for (UIOperatorModel opModel : children) {
            int pointX = opModel.getPosition().getStartX();
            int pointY = opModel.getPosition().getStartY();
            if (pointX > width) {
                width = pointX + 80;
            }
            if (pointY > height) {
                height = pointY + 80;
            }
        }

        //set up graphics for drawing out our image
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ge.createGraphics(img);

        //setting up parameters for drawing the arrows
        g.setColor(Color.BLUE);
        Color b = new Color(0, 0, 255, 100);
        g.setColor(b);
        BasicStroke stroke = new BasicStroke(1);
        g.setStroke(stroke);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        //draw each connection
        List<UIOperatorConnectionModel> connections = ow.getConnModelList();
        for (UIOperatorConnectionModel connModel : connections) {
            OperatorPosition sourcePosition = connModel.getSource().getPosition();
            OperatorPosition targetPosition = connModel.getTarget().getPosition();

            //calculate Position according to the start and end operator
            double[] newPosition = calculatePosition(
                    sourcePosition.getStartX(), sourcePosition.getStartY(),
                    targetPosition.getStartX(), targetPosition.getStartY());

            g.drawLine((int) newPosition[0], (int) newPosition[1], (int) newPosition[2], (int) newPosition[3]);
            drawArrow(newPosition, g);
        }

        //reset graphics as needed for images
        g.setColor(Color.BLACK);

        //draw each operator
        for (UIOperatorModel opModel : children) {
            String name = opModel.getId();
            OperatorPosition position = opModel.getPosition();

            //incase the start x or start y is to small.
            if (position.getStartX() < iconSize / 2) {
                position.setStartX(iconSize / 2);
            }
            if (position.getStartY() < iconSize / 2) {
                position.setStartY(iconSize / 2);
            }
            position.setStartX(position.getStartX() + 40);
            String type = opModel.getClassName();
            drawImage(name, position, type, g);
        }

        //Image is done, now serialize image to system temp folder
        serializeImage(fileId, img);
    }


    private static void serializeImage(String id, BufferedImage img) {
        String resultPath = createFilePath();
        if (resultPath != null) {
            serializeImage(resultPath, id, img);
        }
    }

    private static String createFilePath() {
        String resultPath;
        try {
            File f = new File(FilePersistence.Chorus_PREFIX);
            f = new File(f.getAbsolutePath());
            if (!f.exists()) {
                f.mkdirs();
            }
            resultPath = f.getCanonicalPath();
        } catch (IOException e) {
            itsLogger.error(e.getMessage(), e);
            resultPath = System.getProperty("java.io.tmpdir");
        }
        if (!resultPath.endsWith(File.separator)) {
            resultPath = resultPath + File.separator;
        }
        return resultPath;
    }

    public static String createFilePath(String id, String ext) {
        return createFilePath() + id + ext;
    }

    private static void serializeImage(String resultPath, String id, BufferedImage img) {
        if (id.indexOf(".afm") > 0) {
            id = id.substring(0, id.indexOf(".afm"));
        }

        //save to PNG format, linux temp path has no "/" end
        resultPath = createFilePath(id, ".png");
        try {
            ImageIO.write(img, "PNG", new File(resultPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void drawImage(String name, OperatorPosition position, String type, Graphics g) {
        String imageName = imageNameMap.get(type);
        String iconFile = imageDir + File.separator + imageName;
        try {
            InputStream stream = new FileInputStream(iconFile);
            BufferedImage img = null;
            img = ImageIO.read(stream);
            g.drawImage(img, position.getStartX() - (iconSize + 10), position.getStartY() - iconSize / 2, null);
            g.drawString(name, position.getStartX() - ((iconSize + 10) / 2 + name.length() * 4), position.getStartY() + (iconSize / 2 + 10));

        } catch (Exception e) {
            //just in case an image is missing, we will just draw a rectangle as a placeholder
            itsLogger.warn("Broken image name: " + iconFile);
            itsLogger.error(e.getMessage(), e);
            g.setColor(Color.GREEN);
            g.drawRect(position.getStartX() - (iconSize + 10), position.getStartY() - iconSize / 2, iconSize, iconSize);
            g.setColor(Color.BLACK);
            g.drawString(name, position.getStartX() - ((iconSize + 10) / 2 + name.length() * 4), position.getStartY() + (iconSize / 2 + 10));
        }


    }

    private static void drawArrow(double[] newPosition, Graphics2D g) {
        int arrowTopx = (int) newPosition[2];
        int arrayTopy = (int) newPosition[3];
        int arrowHeadHeight = 5;
        int arrayHeadWidth = 10;

        int xpoints[] = {arrowTopx, arrowTopx - arrayHeadWidth, arrowTopx - arrayHeadWidth};
        int ypoints[] = {arrayTopy, arrayTopy + arrowHeadHeight, arrayTopy - arrowHeadHeight};
        int npoints = 3;
        Color oldColor = g.getColor();
        g.setColor(Color.BLUE);
        g.rotate(newPosition[4], arrowTopx, arrayTopy);
        g.fillPolygon(xpoints, ypoints, npoints);
        g.rotate(-newPosition[4], arrowTopx, arrayTopy);
        g.setColor(oldColor);

    }

    /**
     * Load operators.xml to get icon names
     *
     * @throws Exception
     */
    private static void init(String path) throws Exception {
        XmlDocManager xmlDocManager = new XmlDocManager();
        try {
            imageDir = path;
            itsLogger.debug("icon dir for chorus" + imageDir);
            InputStream stream = new FileInputStream(path + File.separator + "operators.xml");
            xmlDocManager.parseXMLFile(stream);
            ArrayList<Node> operatorNodes = xmlDocManager.getNodeListByTag("Operator");
            for (Node node : operatorNodes) {
                String className = xmlDocManager.getElementValue(node, "ClassName");
                String iconName = xmlDocManager.getElementValue(node, "IconName");
                imageNameMap.put(className, iconName);
            }
        } catch (Exception e) {
            itsLogger.error(e.getMessage(), e);
            throw e;
        }
        inited = true;
    }

    private static boolean alreadyInitialized() {
        return inited;
    }


    /**
     * @param x1 start operator x-axis
     * @param y1 start operator y-axis
     * @param x2 end operator x-axis
     * @param y2 end operator y-axis
     * @return double[5]
     *         double[0] calculated start operator x-axis
     *         double[1] calculated start operator y-axis
     *         double[2] calculated end operator x-axis
     *         double[3] calculated end operator y-axis
     *         double[4] calculated arrow rotation angle
     */
    private static double[] calculatePosition(int x1, int y1, int x2, int y2) {
        double xa, ya, xb, yb;
        double k = 1.0 * (y2 - y1) / (x2 - x1);
        if (Math.abs(k) < 1) {
            if (x1 < x2) {
                //32 is image size
                xa = x1 + iconSize / 2;
                xb = x2 - iconSize / 2;
            } else {
                xa = x1 - iconSize / 2;
                xb = x2 + iconSize / 2;
            }
            ya = (y2 - y1) * (xa - x1) / (x2 - x1) + y1;
            yb = (y2 - y1) * (xb - x1) / (x2 - x1) + y1;
        } else {
            if (y1 < y2) {
                ya = y1 + iconSize / 2;
                yb = y2 - iconSize / 2;
            } else {
                ya = y1 - iconSize / 2;
                yb = y2 + iconSize / 2;
            }
            xa = (x2 - x1) * (ya - y1) / (y2 - y1) + x1;
            xb = (x2 - x1) * (yb - y1) / (y2 - y1) + x1;
        }
        double[] result = new double[5];
        result[0] = xa;
        result[1] = ya;
        result[2] = xb;
        result[3] = yb;

        double rotation = Math.atan(1.0 * (y2 - y1) / (x2 - x1));
        if (x1 > x2) {
            result[4] = rotation + Math.PI;
        } else {
            result[4] = rotation;
        }
        return result;
    }


}
