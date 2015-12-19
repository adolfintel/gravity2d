/*
 * GNU LGPLv3
 */
package com.dosse.gravity2d.demo;

import com.dosse.gravity2d.Point;
import com.dosse.gravity2d.Utils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Federico
 */
public class GUI extends javax.swing.JFrame {

    private Demo d = new Demo();
    private double cameraX = 0, cameraY = 0, zoom = 1;

    private static Color BACKGROUND = new Color(0.02f, 0.02f, 0.15f);
    private static Color POINT_BEFORE_CREATION = new Color(0.5f, 0.5f, 0.5f);

    private final Timer guiUpdater;

    /**
     * Creates new form GUI
     */
    public GUI() {
        initComponents();
        guiUpdater = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (canvas == null) {
                    return;
                }
                if (isActive()) {
                    canvas.requestFocus();
                }
                if (upPressed) {
                    cameraY -= zoom * 3;
                }
                if (downPressed) {
                    cameraY += zoom * 3;
                }
                if (rightPressed) {
                    cameraX += zoom * 3;
                }
                if (leftPressed) {
                    cameraX -= zoom * 3;
                }
                if (creatingAsteroids) {
                    if (oldMouseX != -1) {
                        final double xInSimulation = cameraX + (mouseX - canvas.getWidth() / 2) * zoom + Math.random() * 64 - 32, yInSimulation = cameraY + (mouseY - canvas.getHeight() / 2) * zoom + Math.random() * 64 - 32;
                        d.createAt(xInSimulation, yInSimulation, (mouseX - oldMouseX) * 0.1 * zoom, (mouseY - oldMouseY) * 0.1 * zoom, density * (Math.random() + 0.2), density);
                    }
                    oldMouseX = mouseX;
                    oldMouseY = mouseY;
                } else {
                    oldMouseX = -1;
                    oldMouseY = -1;
                }
                canvas.repaint();
            }
        });
        guiUpdater.setRepeats(true);
        guiUpdater.start();
        setMinimumSize(getSize());
        d.start();
    }

    private int getCPULoad() {
        try {
            com.sun.management.OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class);
            return (int) (osBean.getProcessCpuLoad() * 100);
        } catch (Throwable t) {
            return -2;
        }
    }

    private final Object renderLock = new Object();

    private void render(final Component c, final Graphics g) {
        synchronized (renderLock) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            final int priority = Thread.currentThread().getPriority();
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 3);
            g.setColor(BACKGROUND);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            final double[][] plotData = d.getPlotData();
            final double centerX = -cameraX + (c.getWidth() / 2) * zoom, centerY = -cameraY + (c.getHeight() / 2) * zoom;
            for (double[] pp : plotData) {
                final int diameter = (int) ((pp[2] / zoom) * 2);
                final int px = (int) ((pp[0] + centerX) / zoom) - diameter / 2, py = (int) ((pp[1] + centerY) / zoom) - diameter / 2;
                if (px < -diameter || px > c.getWidth() || py < -diameter || py > c.getHeight()) {
                    continue;
                }
                final float sat = (float) (pp[3] - 1) / 10;
                final float lit = (float) pp[3];
                g.setColor(new Color(Color.HSBtoRGB(0, sat > 1 ? 1 : sat, lit > 1 ? 1 : lit)));
                g.fillOval(px, py, diameter > 1 ? diameter : 1, diameter > 1 ? diameter : 1);
            }
            if (dragging) {
                g.setColor(POINT_BEFORE_CREATION);
                final int diameter = (int) (2 * Math.sqrt(Math.PI * 0.05 * Math.pow(((double) (System.nanoTime() - dragStartT)) / 10000000.0, 2)));
                g.drawOval((int) (dragStartX - diameter / 2), (int) (dragStartY - diameter / 2), diameter, diameter);
                if (planetsMode.isSelected()) {
                    g.drawLine((int) dragStartX, (int) dragStartY, (int) mouseX, (int) mouseY);
                }
            }
            status.setText("Position: " + (int) cameraX + "," + (int) cameraY + " | " + "Scale: " + (int) zoom + " | " + d.getNPoints() + " Points" + " | " + d.getNThreads() + " Threads" + " | " + d.getSPS() + " SPS" + " | " + getCPULoad() + "% CPU");
            Thread.currentThread().setPriority(priority);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton3 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        canvas = new javax.swing.JPanel(){
            public void paint(Graphics g){
                render(canvas,g);
            }
        };
        status = new javax.swing.JLabel();
        planetsMode = new javax.swing.JToggleButton();
        starMode = new javax.swing.JToggleButton();
        asteroidsMode = new javax.swing.JToggleButton();
        resetButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        reset = new javax.swing.JMenuItem();
        load = new javax.swing.JMenuItem();
        save = new javax.swing.JMenuItem();
        quit = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        about = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        bench = new javax.swing.JMenuItem();

        jButton3.setText("jButton3");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Gravity Simulator");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        canvas.setFocusCycleRoot(true);
        canvas.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                canvasMouseDragged(evt);
            }
        });
        canvas.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                canvasMouseWheelMoved(evt);
            }
        });
        canvas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                canvasMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                canvasMouseReleased(evt);
            }
        });
        canvas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                canvasKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                canvasKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                canvasKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout canvasLayout = new javax.swing.GroupLayout(canvas);
        canvas.setLayout(canvasLayout);
        canvasLayout.setHorizontalGroup(
            canvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        canvasLayout.setVerticalGroup(
            canvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 384, Short.MAX_VALUE)
        );

        status.setText("Loading...");
        status.setFocusable(false);

        planetsMode.setSelected(true);
        planetsMode.setText("Planet");
        planetsMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                planetsModeActionPerformed(evt);
            }
        });

        starMode.setText("Star");
        starMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                starModeActionPerformed(evt);
            }
        });

        asteroidsMode.setText("Asteroids");
        asteroidsMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                asteroidsModeActionPerformed(evt);
            }
        });

        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(canvas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(status, javax.swing.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(planetsMode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(starMode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(asteroidsMode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(resetButton)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(canvas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(status)
                .addGap(9, 9, 9)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(planetsMode)
                    .addComponent(starMode)
                    .addComponent(asteroidsMode)
                    .addComponent(resetButton))
                .addContainerGap())
        );

        getContentPane().add(jPanel1);

        jMenu1.setText("File");

        reset.setText("New simulation");
        reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetActionPerformed(evt);
            }
        });
        jMenu1.add(reset);

        load.setText("Load...");
        load.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadActionPerformed(evt);
            }
        });
        jMenu1.add(load);

        save.setText("Save...");
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });
        jMenu1.add(save);

        quit.setText("Quit");
        quit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitActionPerformed(evt);
            }
        });
        jMenu1.add(quit);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("?");

        about.setText("About...");
        about.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutActionPerformed(evt);
            }
        });
        jMenu2.add(about);

        jMenu3.setText("Extras");

        bench.setText("Benchmark");
        bench.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                benchActionPerformed(evt);
            }
        });
        jMenu3.add(bench);

        jMenu2.add(jMenu3);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private boolean leftPressed, rightPressed, upPressed, downPressed;

    private void canvasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_canvasKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
            leftPressed = true;
        }
        if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        }
        if (evt.getKeyCode() == KeyEvent.VK_UP) {
            upPressed = true;
        }
        if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            downPressed = true;
        }
        if (evt.getKeyCode() == KeyEvent.VK_R) {
            resetActionPerformed(null);
        }
        if (evt.getKeyCode() == KeyEvent.VK_P) {
            d.pause = !d.pause;
        }
    }//GEN-LAST:event_canvasKeyPressed

	private void canvasKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_canvasKeyReleased
            if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                leftPressed = false;
            }
            if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                rightPressed = false;
            }
            if (evt.getKeyCode() == KeyEvent.VK_UP) {
                upPressed = false;
            }
            if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                downPressed = false;
            }
    }//GEN-LAST:event_canvasKeyReleased

    private double dragStartX, dragStartY, mouseX, mouseY;
    private long dragStartT;
    private boolean dragging = false, creatingAsteroids = false;
    private double oldMouseX = -1, oldMouseY = -1;
    private double density = 1;
    private void canvasMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_canvasMousePressed
        mouseX = evt.getX();
        mouseY = evt.getY();
        if (planetsMode.isSelected() || starMode.isSelected()) {
            dragStartX = evt.getX();
            dragStartY = evt.getY();
            dragStartT = System.nanoTime();
            dragging = true;
        }
        if (asteroidsMode.isSelected()) {
            creatingAsteroids = true;
        }
    }//GEN-LAST:event_canvasMousePressed

    private void canvasMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_canvasMouseReleased
        mouseX = evt.getX();
        mouseY = evt.getY();
        if (planetsMode.isSelected()) {
            final double dx = evt.getX() - dragStartX, dy = evt.getY() - dragStartY;
            final double xInSimulation = cameraX + (dragStartX - canvas.getWidth() / 2) * zoom, yInSimulation = cameraY + (dragStartY - canvas.getHeight() / 2) * zoom;
            d.createAt(xInSimulation, yInSimulation, dx * 0.01 * zoom, dy * 0.01 * zoom, 0.05 * density * Math.pow(((double) (System.nanoTime() - dragStartT)) / 10000000.0, 2) * zoom * zoom, density);
            dragging = false;
        }
        if (starMode.isSelected()) {
            final double xInSimulation = cameraX + (dragStartX - canvas.getWidth() / 2) * zoom, yInSimulation = cameraY + (dragStartY - canvas.getHeight() / 2) * zoom;
            d.createAt(xInSimulation, yInSimulation, 0, 0, 0.05 * density * Math.pow(((double) (System.nanoTime() - dragStartT)) / 10000000.0, 2) * zoom * zoom, density);
            dragging = false;
        }
        if (asteroidsMode.isSelected()) {
            creatingAsteroids = false;
        }
    }//GEN-LAST:event_canvasMouseReleased


    private void canvasMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_canvasMouseDragged
        mouseX = evt.getX();
        mouseY = evt.getY();
    }//GEN-LAST:event_canvasMouseDragged

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        guiUpdater.stop();
        d.stopASAP = true;
        dispose();
    }//GEN-LAST:event_formWindowClosing

    private void canvasKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_canvasKeyTyped
        if (evt.getKeyCode() == KeyEvent.VK_R) {
            d.reset();
            cameraX = 0;
            cameraY = 0;
            zoom = 1;
        }
    }//GEN-LAST:event_canvasKeyTyped

    private void canvasMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_canvasMouseWheelMoved
        zoom += evt.getUnitsToScroll() > 0 ? zoom < 300 ? 1 : 0 : zoom > 1 ? -1 : 0;
    }//GEN-LAST:event_canvasMouseWheelMoved
    private static final FileFilter SAVEFILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".gds");
        }

        @Override
        public String getDescription() {
            return "GravityDemo saved state (*.gds)";
        }
    };
    private void loadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadActionPerformed
        JFileChooser c = new JFileChooser();
        c.setFileFilter(SAVEFILE_FILTER);
        c.setMultiSelectionEnabled(false);
        c.showOpenDialog(rootPane);
        File x = c.getSelectedFile();
        if (x == null) {
            return;
        }
        try {
            Point[] state;
            double cx, cy;
            double z;
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(x));
            state = (Point[]) (ois.readObject());
            cx = ois.readDouble();
            cy = ois.readDouble();
            z = ois.readDouble();
            ois.close();
            d.loadState(state);
            cameraX = cx;
            cameraY = cy;
            zoom = z;
        } catch (Throwable ex) {
            JOptionPane.showMessageDialog(new JOptionPane(), "Invalid save file\n" + ex, getTitle(), JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_loadActionPerformed

    private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
        Point[] state = d.saveState();
        JFileChooser c = new JFileChooser();
        c.setFileFilter(SAVEFILE_FILTER);
        c.setMultiSelectionEnabled(false);
        c.showSaveDialog(rootPane);
        File x = c.getSelectedFile();
        if (x == null) {
            return;
        } else if (!x.getName().toLowerCase().endsWith(".gds")) {
            x = new File(x.getAbsolutePath() + ".gds");
        }
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(x));
            oos.writeObject(state);
            oos.writeDouble(cameraX);
            oos.writeDouble(cameraY);
            oos.writeDouble(zoom);
            oos.close();
        } catch (Throwable ex) {
            JOptionPane.showMessageDialog(rootPane, "Save failed\n" + ex, getTitle(), JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_saveActionPerformed

    private void resetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetActionPerformed
        d.reset();
        cameraX = 0;
        cameraY = 0;
        zoom = 1;
    }//GEN-LAST:event_resetActionPerformed

    private void quitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitActionPerformed
        formWindowClosing(null);
    }//GEN-LAST:event_quitActionPerformed

    private void benchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_benchActionPerformed
        formWindowClosing(null);
        while (d.isAlive()) {
            Utils.releaseCPU();
        }
        new BenchmarkGUI().setVisible(true);
    }//GEN-LAST:event_benchActionPerformed

    private void aboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutActionPerformed
        JOptionPane.showMessageDialog(rootPane, "Gravity Simulator, a multithreaded N-Point gravity simulator\nDeveloped by Federico Dossena.", getTitle(), JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_aboutActionPerformed

    private void planetsModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_planetsModeActionPerformed
        density = 1;
        starMode.setSelected(false);
        asteroidsMode.setSelected(false);
    }//GEN-LAST:event_planetsModeActionPerformed

    private void starModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_starModeActionPerformed
        density = 10;
        planetsMode.setSelected(false);
        asteroidsMode.setSelected(false);
    }//GEN-LAST:event_starModeActionPerformed

    private void asteroidsModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_asteroidsModeActionPerformed
        density = 1;
        starMode.setSelected(false);
        planetsMode.setSelected(false);
    }//GEN-LAST:event_asteroidsModeActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        resetActionPerformed(null);
    }//GEN-LAST:event_resetButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Throwable ex) {
         }*/
        new GUI().setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem about;
    private javax.swing.JToggleButton asteroidsMode;
    private javax.swing.JMenuItem bench;
    private javax.swing.JPanel canvas;
    private javax.swing.JButton jButton3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JMenuItem load;
    private javax.swing.JToggleButton planetsMode;
    private javax.swing.JMenuItem quit;
    private javax.swing.JMenuItem reset;
    private javax.swing.JButton resetButton;
    private javax.swing.JMenuItem save;
    private javax.swing.JToggleButton starMode;
    private javax.swing.JLabel status;
    // End of variables declaration//GEN-END:variables
}
