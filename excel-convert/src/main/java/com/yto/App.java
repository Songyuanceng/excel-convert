package com.yto;

import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.text.ParseException;
import java.util.HashSet;

/**
 * Hello world!
 *
 */
public class App {

    static JProgressBar ax;

    public static void main( String[] args ) {
        try {
            BeautyEyeLNFHelper.launchBeautyEyeLNF();
            UIManager.put("RootPane.setupButtonVisible", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFrame jframe = new JFrame("圆通excel转换工具");
        JPanel jPanel = new JPanel();
        // 设置 contentPane 属性。
        jPanel.setLayout(null);
        jframe.setContentPane(jPanel);
        jframe.setVisible(true);
        jframe.setSize(400, 250);
        jframe.setLocation(800,300);
        jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // 创建一个Label标签
        JLabel jl = new JLabel("选择模板：");
        // 样式，让文字居中
        jl.setHorizontalAlignment(SwingConstants.LEFT);
        jl.setBounds(70,25,100,25);
        // 将标签添加到容器中
        jPanel.add("North", jl);
        JButton developer = new JButton("导入模板");
        developer.setBounds(150,25,100,25);
        developer.setHorizontalAlignment(SwingConstants.CENTER);
        jPanel.add(developer);

        ax = new JProgressBar(1,30);
        ax.setBorderPainted(false);
        ax.setStringPainted(true);
        ax.setBounds(0,150,400,25);
        ax.setString("");
      //  ax.setForeground(Color.gray);
        jPanel.add(ax);
        JLabel desc = new JLabel("使用说明:");
        desc.setBounds(50,75,200,25);
        desc.setForeground(Color.red);
        JLabel desc1 = new JLabel("1.选择正确得excel模板导入工具");
        desc1.setBounds(50,100,300,25);
        desc1.setForeground(Color.red);
        JLabel desc2 = new JLabel("2.即可在C:/customs/photo生成对应得文件");
        desc2.setBounds(50,125,300,25);
        desc2.setForeground(Color.red);
        // 将标签添加到容器中
        jPanel.add(BorderLayout.SOUTH, desc);
        jPanel.add(BorderLayout.SOUTH, desc1);
        jPanel.add(BorderLayout.SOUTH, desc2);
        developer.addMouseListener(new MouseAdapter() {
            // 添加鼠标点击事件
        @Override
        public void mouseClicked(MouseEvent event) {

            SwingWorker testWorker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    try {
                        excelConvert(new JButton());
                    } catch (FileNotFoundException e) {
                        JOptionPane.showMessageDialog(jPanel, e.getMessage() ," 错误", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    } catch (ParseException e) {
                        JOptionPane.showMessageDialog(jPanel, "模板解析异常" ," 错误", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }catch (Exception e){
                        JOptionPane.showMessageDialog(jPanel, "模板解析异常" ," 错误", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            testWorker.execute();
          }
        });
    }

    /**
       * 文件上传功能
       * 
       * @param developer
       *  按钮控件名称
       */
    public static void eventOnImport(JButton developer) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        /** 过滤文件类型 * */
        FileNameExtensionFilter filter = new FileNameExtensionFilter("xls", "xlsx");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(developer);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            /** 得到选择的文件* */
        File[] arrfiles = chooser.getSelectedFiles();
        if (arrfiles == null || arrfiles.length == 0) {
             return;
        }
        FileInputStream input = null;
        FileOutputStream out = null;
        String path = "./";
        try {
            for (File f : arrfiles) {
                File dir = new File(path);
                /** 目标文件夹 * */
                File[] fs = dir.listFiles();
                HashSet<String> set = new HashSet<String>();
                for (File file : fs) {
                set.add(file.getName());
            }
            /** 判断是否已有该文件* */
            if (set.contains(f.getName())) {
                JOptionPane.showMessageDialog(new JDialog(),
                f.getName() + ":该文件已存在！");
                 return;
            }
            input = new FileInputStream(f);
            byte[] buffer = new byte[1024];
            File des = new File(path, f.getName());
            out = new FileOutputStream(des);
            int len = 0;
            while (-1 != (len = input.read(buffer))) {
               out.write(buffer, 0, len);
            }
             out.close();
            input.close();
            }
            JOptionPane.showMessageDialog(null, "上传成功！", "提示",
                                JOptionPane.INFORMATION_MESSAGE);

        } catch (FileNotFoundException e1) {
            JOptionPane.showMessageDialog(null, "上传失败！", "提示",
                            JOptionPane.ERROR_MESSAGE);
            e1.printStackTrace();
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(null, "上传失败！", "提示",
                            JOptionPane.ERROR_MESSAGE);
             e1.printStackTrace();
        }
     }
  }
    /**
       * excel转换
       * @param developer
       *  按钮控件名称
       */

    public static void excelConvert(JButton excel) throws FileNotFoundException, ParseException {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        /** 过滤文件类型 * */
        FileNameExtensionFilter filter = new FileNameExtensionFilter("xls", "xlsx");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(excel);
        ax.setString("开始解析excel...");
        ax.setValue(1);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            /** 得到选择的文件* */
            File[] arrfiles = chooser.getSelectedFiles();
            if (arrfiles == null || arrfiles.length == 0) {
                return;
            }
            String path = "./";
            for (File f : arrfiles) {
                File dir = new File(path);
                /** 目标文件夹 * */
                File[] fs = dir.listFiles();
                HashSet<String> set = new HashSet<>();
                for (File file : fs) {
                    set.add(file.getName());
                }
                /** 判断是否已有该文件* */
                if (set.contains(f.getName())) {
                    JOptionPane.showMessageDialog(new JDialog(),
                            f.getName() + ":该文件已存在！");
                    return;
                }
                /*List<ExportTest> list=ExcelUtils.readExcel(ExportTest.class,f);
                for (ExportTest exportTest : list) {
                    System.out.println(exportTest);
                }*/
                FileUtil.createFile(f,ax);
            }
        }
    }

}
