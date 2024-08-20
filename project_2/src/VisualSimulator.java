// VisualSimulator.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class VisualSimulator {
    private int currentIndex;
    private SicXeSimulator simulator;
    private ArrayList<TextInfo> textInfos;
    private JTextField fileNameField;
    private JList<String> logField;
    private JList<String> instructionField;
    private DefaultListModel<String> logModel;
    private DefaultListModel<String> instructionModel;
    private JTextField programName;
    private JTextField programLength;
    private JTextField programStartAddr;
    private JTextField decA;
    private JTextField decX;
    private JTextField decL;
    private JTextField decPC;
    private JTextField decSW;
    private JTextField hexA;
    private JTextField hexX;
    private JTextField hexL;
    private JTextField hexPC;
    private JTextField hexSW;
    private JTextField hexB;
    private JTextField decB;
    private JTextField decS;
    private JTextField hexS;
    private JTextField hexF;
    private JTextField decF;
    private JTextField hexT;
    private JTextField decT;
    private JTextField addrFirstInst;
    private JTextField startAddrMem;
    private JTextField targetAddr;
    private JTextField usedDevice;
    private JButton oneStopBtn;
    private JButton allBtn;
    private JButton endBtn;
    private JButton fileBtn;

    static File chosenFile;

    public VisualSimulator() {
        simulator = new SicXeSimulator();
        currentIndex = 0;
        textInfos = new ArrayList<>();
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("SIC/XE Simulator");
        frame.setTitle("SIC/XE Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 800);

        // northPanel
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel fileLabel = new JLabel("FileName: ");
        fileNameField = new JTextField(20);
        fileNameField.setEditable(false);
        northPanel.add(fileLabel);
        northPanel.add(fileNameField);
        fileBtn = new JButton("open");
        northPanel.add(fileBtn);
        /* file open button */
        fileBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser chooser = new JFileChooser(); // 파일 선택 대화상자 생성
                int ret = chooser.showOpenDialog(null);
                if (ret == JFileChooser.APPROVE_OPTION) { // 사용자가 파일을 선택하고 확인 버튼을 클릭한 경우
                    try {
                        if (chooser.getSelectedFile() == null) {
                            chosenFile = Paths.get("C:\\Users\\sshl5\\IdeaProjects\\source\\project_2\\src\\apple.txt").toFile(); // default 파일로 src/proj_2.txt를 설정
                        } else {
                            chosenFile = Paths.get("C:\\Users\\sshl5\\IdeaProjects\\source\\project_2\\src\\apple.txt").toFile();
                            //chosenFile = chooser.getSelectedFile(); // 사용자가 선택한 파일을 필드 변수에 저장
                        }
                        fileNameField.setText(chosenFile.getName());
                        String objectCode = new String(Files.readAllBytes(Paths.get("C:\\Users\\sshl5\\IdeaProjects\\source\\project_2\\src\\output_objectcode_ex.txt")));
                        simulator.loadObjectCode(objectCode);
                        // objectcode를 메모리에 load
                        textInfos = simulator.executeInstruction();
                        // simulate
                        initialize();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // centerPanel
        JPanel centerPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        JPanel headerPanel = createPanelHeader(new JPanel());
        JPanel registerPanel = createPanelRegister(new JPanel());
        JPanel xeregisterPanel = createPanelRegister2(new JPanel());

        JPanel endPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        endPanel.add(new JLabel("Address of First Instruction in Object Program:"));
        addrFirstInst = addLabelAndTextField(endPanel, 5);

        JPanel addressPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        addressPanel.add(new JLabel("Start Address in Memory"));
        startAddrMem = addLabelAndTextField(addressPanel, 5);
        JPanel targetPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        targetPanel.add(new JLabel("Target Address:"));
        targetAddr = addLabelAndTextField(targetPanel, 5);
        addressPanel.add(targetPanel);
        addressPanel.add(new JLabel("Instructions:"));
        JPanel instPanel = createPanelInst(new JPanel());

        headerPanel.setBorder(BorderFactory.createTitledBorder("H(Header Record"));
        endPanel.setBorder(BorderFactory.createTitledBorder("E(End Record)"));
        registerPanel.setBorder(BorderFactory.createTitledBorder("Register"));
        xeregisterPanel.setBorder(BorderFactory.createTitledBorder("Register(for XE)"));

        centerPanel.add(headerPanel);
        centerPanel.add(endPanel);
        centerPanel.add(registerPanel);
        centerPanel.add(addressPanel);
        centerPanel.add(xeregisterPanel);
        centerPanel.add(instPanel);

        // southPanel
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        JLabel logLabel = new JLabel("Log(명령어 수행 관련):");
        logModel = new DefaultListModel<>();
        logField = new JList<>(logModel);
        JScrollPane logScrollPane = new JScrollPane(logField);
        southPanel.add(logLabel, BorderLayout.WEST);
        southPanel.add(logScrollPane, BorderLayout.CENTER);

        frame.add(northPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(southPanel, BorderLayout.SOUTH);

        /* one Stop execute button */
        oneStopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (currentIndex < textInfos.size()) {
                        update(textInfos, currentIndex);
                        currentIndex += 1;
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        /* all execute button */
        allBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    logModel.clear(); // Jlist 기존 내용 clear
                    instructionModel.clear();
                    for (int i = 0; i < textInfos.size(); i++) {
                        update(textInfos, i);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        /* end button */
        endBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirmed = JOptionPane.showConfirmDialog(null,
                        "프로그램을 종료하시겠습니까?", "종료 확인",
                        JOptionPane.YES_NO_OPTION);
                if (confirmed == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
        frame.setVisible(true);
    }

    /* panel구성에 쓰인 함수입니다.*/
    private JPanel createPanelInst(JPanel panel) {
        panel.setLayout(new GridLayout(1, 2, 5, 5));
        instructionModel = new DefaultListModel<>();
        instructionField = new JList<>(instructionModel);
        JScrollPane instructionScrollPane = new JScrollPane(instructionField);
        panel.add(instructionScrollPane);
        JPanel anotherPane = new JPanel(new GridLayout(6, 1, 5, 5));
        anotherPane.add(new JLabel("사용 중인 장치"));
        usedDevice = addLabelAndTextField(anotherPane, 5);
        anotherPane.add(new JPanel());
        oneStopBtn = new JButton("실행(1step)");
        anotherPane.add(oneStopBtn);
        allBtn = new JButton("실행(All)");
        anotherPane.add(allBtn);
        endBtn = new JButton("종료");
        anotherPane.add(endBtn);
        panel.add(anotherPane);
        return panel;
    }
    private JPanel createPanelRegister(JPanel panel) {
        panel.setLayout(new GridLayout(6, 3, 5, 5));
        panel.add(new JPanel());
        panel.add(new JLabel("dec"));
        panel.add(new JLabel("hex"));
        panel.add(new JLabel("A(#0)"));
        decA = addLabelAndTextField(panel, 7);
        hexA = addLabelAndTextField(panel, 7);
        panel.add(new JLabel("X(#1)"));
        decX = addLabelAndTextField(panel, 7);
        hexX = addLabelAndTextField(panel, 7);
        panel.add(new JLabel("L(#2)"));
        decL = addLabelAndTextField(panel, 7);
        hexL = addLabelAndTextField(panel, 7);
        panel.add(new JLabel("PC(#8)"));
        decPC = addLabelAndTextField(panel, 7);
        hexPC = addLabelAndTextField(panel, 7);
        panel.add(new JLabel("SW(#9)"));
        decSW = addLabelAndTextField(panel, 7);
        hexSW = addLabelAndTextField(panel, 7);
        return panel;
    }
    private JPanel createPanelRegister2(JPanel panel) {
        panel.setLayout(new GridLayout(5, 3, 5, 5));
        panel.add(new JPanel());
        panel.add(new JLabel("dec"));
        panel.add(new JLabel("hex"));
        panel.add(new JLabel("B(#3)"));
        decB = addLabelAndTextField(panel, 7);
        hexB = addLabelAndTextField(panel, 7);
        panel.add(new JLabel("S(#4)"));
        decS = addLabelAndTextField(panel, 7);
        hexS = addLabelAndTextField(panel, 7);
        panel.add(new JLabel("T(#5)"));
        decT = addLabelAndTextField(panel, 7);
        hexT = addLabelAndTextField(panel, 7);
        panel.add(new JLabel("F(#6)"));
        decF = addLabelAndTextField(panel, 7);
        hexF = addLabelAndTextField(panel, 7);
        return panel;
    }
    private JPanel createPanelHeader(JPanel panel) {
        panel.setLayout(new GridLayout(3, 2, 5, 5)); // Use GridLayout with 3 rows, 2 columns
        panel.add(new JLabel("Program Name: "));
        programName = addLabelAndTextField(panel, 7);
        panel.add(new JLabel("Start Address of Object Program: "));
        programStartAddr = addLabelAndTextField(panel, 7);
        panel.add(new JLabel("Length of Program: "));
        programLength = addLabelAndTextField(panel, 7);
        return panel;
    }
    private JTextField addLabelAndTextField(JPanel panel, int textFieldColumns) {
        JTextField textField = new JTextField(textFieldColumns);
        if (textFieldColumns != 0) {
            panel.add(textField);
        }
        return textField;
    }
    /* panel구성에 쓰인 함수 */

    /* 초기 화면 구성 함수 */
    public void initialize() {
        ArrayList<String> header = simulator.getHeader();
        programName.setText(header.get(0));
        programStartAddr.setText(header.get(1));
        programLength.setText(header.get(2));
        addrFirstInst.setText(header.get(3));
        setReg(decA, hexA, 0);
        setReg(decX, hexX, 0);
        setReg(decL, hexL, 0);
        setReg(decPC, hexPC, 0);
        setReg(decSW, hexSW, 0);
        setReg(decB, hexB, 0);
        setReg(decT, hexT, 0);
        setReg(decS, hexS, 0);
        setReg(decF, hexF, 0);
    }
    /* 화면 업데이트 함수 */
    public void update(ArrayList<TextInfo> textInfo, int index) {
        TextInfo currentInfo = textInfo.get(index);
        setReg(decA, hexA, currentInfo.getValueA());
        setReg(decX, hexX, currentInfo.getValueX());
        setReg(decL, hexL, currentInfo.getValueL());
        setReg(decPC, hexPC, currentInfo.getValuePC());
        setReg(decSW, hexSW, currentInfo.getValueSW());
        setReg(decB, hexB, currentInfo.getValueB());
        setReg(decT, hexT, currentInfo.getValueT());
        setReg(decS, hexS, currentInfo.getValueS());
        setReg(decF, hexF, currentInfo.getValueF());
        usedDevice.setText(currentInfo.getTestDevice());
        if(index==0){
            logModel.addElement("START LOG");
            instructionModel.addElement("start~!");
            logModel.addElement("Executed instruction : " + currentInfo.getCurrenLog());
            instructionModel.addElement(currentInfo.getCurrentInst());
        }else if(index==textInfos.size()-1){
            logModel.addElement("Executed instruction : " + currentInfo.getCurrenLog());
            instructionModel.addElement(currentInfo.getCurrentInst());
            logModel.addElement("END LOG");
            instructionModel.addElement("end~!");
        }else{
            logModel.addElement("Executed instruction : " + currentInfo.getCurrenLog());
            instructionModel.addElement(currentInfo.getCurrentInst());
        }
    }
    /* dec,hex값 match 함수 */
    public void setReg(JTextField jTextField1, JTextField jTextField2, int regNum) {
        jTextField1.setText(String.format("%d", regNum));
        jTextField2.setText(String.format("%06x", regNum));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VisualSimulator::new);
    }
}
