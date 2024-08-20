import java.io.*;
import java.util.ArrayList;
import java.util.Optional;

public class InstructionExecutor {
    private int PC;
    private String testDevice;
    private boolean usedChance=false; // 프로그램을 모두 실행시켰는지를 알 수 있는 flag
    private File newFile; // 선택된 파일을 copy할 새로운 file
    private ArrayList<TextInfo> textInfos; // GUI에 전달할 내용들을 담은 TextInfos객체 배열
    private BufferedReader fileReader;
    private BufferedWriter fileWriter;
    private InstructionTable instructionTable;
    public InstructionExecutor() {
        PC = 0;
        newFile = new File("new_file.txt");
        textInfos = new ArrayList<>();
    }
    public void setInstructionTable() throws IOException {
        instructionTable = new InstructionTable("C:\\Users\\sshl5\\IdeaProjects\\source\\project_2\\src\\inst_table.txt");
    }
    public void setFileReader() throws IOException {
        fileReader = new BufferedReader(new FileReader(VisualSimulator.chosenFile));
    }
    public void setFileWriter() throws IOException {
        fileWriter = new BufferedWriter(new FileWriter(newFile));
    }
    /* textInfo를 제작하는 함수 == textInfo의 setter */
    public void addTextInfo(ResourceManager resourceManager,String instName,String instCode,String testDevice){
        TextInfo newTextInfo = new TextInfo();
        newTextInfo.setValueA(resourceManager.getRegister(0));
        newTextInfo.setValueX(resourceManager.getRegister(1));
        newTextInfo.setValueL(resourceManager.getRegister(2));
        newTextInfo.setValuePC(resourceManager.getRegister(8));
        newTextInfo.setValueSW(resourceManager.getRegister(9));
        newTextInfo.setValueB(resourceManager.getRegister(3));
        newTextInfo.setValueS(resourceManager.getRegister(4));
        newTextInfo.setValueT(resourceManager.getRegister(5));
        newTextInfo.setValueF(resourceManager.getRegister(6));
        newTextInfo.setCurrentInst(instCode);
        newTextInfo.setCurrenLog(instName);
        newTextInfo.setTestDevice(testDevice);
        textInfos.add(newTextInfo);
    }
    /* textInfo의 getter */
    public ArrayList<TextInfo> getTextInfos() {
        return textInfos;
    }
    /* instruction simulation하는 함수 */
    public void executeInstruction(int start_address, ResourceManager resourceManager) throws Exception {
        PC=start_address;
        setInstructionTable();
        setFileReader();
        setFileWriter();
        testDevice = "no device";

        while (true) {

            if(PC==start_address){
                if(!usedChance){
                    usedChance = true;
                }else{
                    break;
                }
            }
            byte currentByte = resourceManager.getMemory(PC);

            int opcode = (currentByte & 0xFC);
            String name; // instruction의 이름
            String instCode; // 실행되는 object code
            InstructionInfo instructionInfo;
            int indexAidLen = 3; // for문에 사용되는 변수

            if (instructionTable.searchOpcode(opcode).isPresent()) {
                instructionInfo = instructionTable.searchOpcode(opcode).get();
                name = instructionInfo.getName();
            } else {
                throw new Exception("inst.txt 파일에 없는 명령어입니다."); // inst.txt 파일에 없는 명령어인 경우 예외처리
            }

            byte nextByte = resourceManager.getMemory(PC+1);
            int nixbpe = ((currentByte & 0x03) << 8 ) | (nextByte & 0xFF); // nixbpe 비트들을 가져옵니다
            int eBit = (nixbpe >> 4) & 1;
            int nBit = (nixbpe >> 9) & 1;
            int mBit = (nixbpe >> 8) & 1;
            int xBit = (nixbpe >> 7) & 1;
            int targetAddr=0; // (TA)에 해당합니다

            if(instructionInfo.getFormat()==2) { // format 2
                int instCodeNum = (currentByte & 0xFF)<<8 | (nextByte & 0xFF);
                instCode = String.format("%04X",instCodeNum);
            }
            else if((mBit==1 && nBit==0 )|| (mBit==0 && nBit==1)){ // immediate addressing, indirect addressing
                int instCodeNum = (currentByte & 0xFF) <<16 | (nextByte & 0xFF) << 8 | (resourceManager.getMemory(PC + 2) & 0xFF);
                instCode = String.format("%06X",instCodeNum);
                targetAddr = ((nextByte & 0x0F) << 8) | (resourceManager.getMemory(PC + 2) & 0xFF);
            }
            else if(eBit==1){ // direct addressing
                int instCodeNum = (currentByte & 0xFF)<<24 | (nextByte & 0xFF) <<16 | (resourceManager.getMemory(PC + 2) & 0xFF) << 8 | (resourceManager.getMemory(PC + 3) & 0xFF);
                instCode = String.format("%08X",instCodeNum);
                targetAddr = ((nextByte & 0x0F) << 16) | ((resourceManager.getMemory(PC + 2) & 0xFF) << 8) | (resourceManager.getMemory(PC + 3) & 0xFF);
            }
            else{ // relative addressing
                int instCodeNum = (currentByte & 0xFF) <<16 | (nextByte & 0xFF) << 8 | (resourceManager.getMemory(PC + 2) & 0xFF);
                instCode = String.format("%06X",instCodeNum);
                targetAddr = ((nextByte & 0x0F) << 8) | (resourceManager.getMemory(PC + 2) & 0xFF);
                if ((targetAddr & 0x800) != 0) {
                    targetAddr |= 0xFFFFF000; // 음수일 때도 처리
                }
                targetAddr += (PC+3); // PC relative처리
            }

            if(xBit==1){
                indexAidLen = resourceManager.getRegister(1); // indexed mode 처리
            }
            
            if(name.startsWith("ST")){
                char regChar = name.charAt(2);
                int regValue = 0;
                if(name.equals("STCH")){
                    regValue = resourceManager.getRegister(0);
                    resourceManager.setMemory(targetAddr + indexAidLen, (byte)(regValue & 0xFF)); // (레지스터) -> (m)
                }
                else {
                    regValue = resourceManager.getRegister(getRegisterNumber(regChar));
                    for (int i = 0; i < indexAidLen; i++) {
                        byte valueToStore = (byte) (regValue & 0xFF);
                        resourceManager.setMemory(targetAddr + i, valueToStore); // (레지스터) -> (m...m+2)
                        regValue = regValue >> 8; // 다음 바이트로 이동
                    }
                }
            }else if(name.startsWith("J") || name.endsWith("SUB")){
                boolean canJump=false;
                if(name.equals("RSUB")){
                    PC = resourceManager.getRegister(2); // PC <- (L)
                    resourceManager.setRegister(8,PC); // PC바뀌면 8번 레지스터에 저장(PC reg)
                    addTextInfo(resourceManager,name,instCode,testDevice); // 리턴 전, textInfo 저장
                    continue;
                }
                else if(name.equals("JSUB")){
                    if(eBit==1){
                        resourceManager.setRegister(2,PC+4);// L <- (PC)
                    }else{
                        resourceManager.setRegister(2,PC+3);// L <- (PC)
                    }
                    PC = targetAddr; // PC <- m
                    resourceManager.setRegister(8,PC); // PC바뀌면 8번 레지스터에 저장(PC reg)
                    addTextInfo(resourceManager,name,instCode,testDevice);
                    continue;
                }else{
                    canJump=executeJump(name,resourceManager,targetAddr);
                }
                if(canJump){ // jump 실패 시, 계속 진행되어야 함
                    addTextInfo(resourceManager,name,instCode,testDevice);
                    continue;
                }
            }else if(name.startsWith("LD")){
                char regChar = name.charAt(2);
                int memoryValue =0;
                if(name.equals("LDCH")) {
                    regChar = 'A';
                    memoryValue = resourceManager.getMemory(targetAddr+indexAidLen); // 메모리에서 1 byte 가져오기
                }
                else if(mBit==1 && nBit==0){
                    memoryValue=targetAddr;
                }else{
                    for(int i=indexAidLen-1;i>=0;i--){
                        memoryValue |= (resourceManager.getMemory(targetAddr + i) & 0xFF) << (8*i); // 메모리에서 3 byte 가져오기
                    }
                }
                resourceManager.setRegister(getRegisterNumber(regChar),memoryValue); // reg값 <- m

            }else if(name.equals("TD") || name.equals("WD") || name.equals("RD")) {
                if(name.equals("TD")){
                    byte memorybyte= (byte) (resourceManager.getMemory(targetAddr) & 0xFF);
                    testDevice = String.format("%02X",memorybyte);
                }
                executeDevice(name, resourceManager);
            }else if(name.endsWith("R") && instructionInfo.getFormat()==2){
                int regNum1 = (nextByte >> 4) & 0x0F; // 상위 4비트 = r1에 해당
                int regNum2 = nextByte & 0x0F; //하위 4비트 r2에 해당
                executeRegister(name,regNum1,regNum2,resourceManager);
            }else if(name.equals("COMP")){
                int memoryValue=0;
                if(nBit==0&&mBit==1){
                    memoryValue = targetAddr;
                }else{
                    for(int i=indexAidLen-1;i>=0;i--){
                        memoryValue |= (resourceManager.getMemory(targetAddr + i) & 0xFF) << (8*i);
                    }
                }
                int regvalue = resourceManager.getRegister(0); // A레지스터의 값 가져옴 => COMP이기 떄문
                resourceManager.setRegister(9,regvalue-memoryValue);
            }else{
                throw new Exception("처리할 수 없는 명령어입니다.");
            }

            if(instructionInfo.getFormat()==2){
                PC+=2;
            }
            else if(eBit==1){
                PC+=4;
            }else{
                PC+=3;
            }
            // PC 업데이트
            resourceManager.setRegister(8,PC); // PC 레지스터 업데이트
            addTextInfo(resourceManager,name,instCode,testDevice); // testInfo값 추가
        }
        System.out.println("Success in!");

        // 파일관련 처리
        fileReader.close();
        fileWriter.flush();
        fileWriter.close();
    }

    /*jump관련 처리*/
    public boolean executeJump(String name,ResourceManager resourceManager,int targetAddr){
        if(name.equals("JEQ") && resourceManager.getRegister(9)==0){
            resourceManager.setRegister(8,PC); // PC 레지스터에 값 저장
            PC = targetAddr;
            return true;
        }else if(name.equals("JLT") && resourceManager.getRegister(9)<0) {
            resourceManager.setRegister(8,PC); // PC 레지스터에 값 저장
            PC = targetAddr;
            return true;
        }
        else if(name.equals("J")){
            resourceManager.setRegister(8,PC); // PC 레지스터에 값 저장
            PC = targetAddr;
            return true;
        }
        return false;
    }
    /*register 관련 처리*/
    public void executeRegister(String name,int r1, int r2,ResourceManager resourceManager){
        if (name.equals("ADDR")){
            resourceManager.setRegister(r2,r1+r2); // (r2) <- (r1) + (r2)
        }else if(name.equals("CLEAR")){
            resourceManager.setRegister(r1,0);
        }else if(name.equals("COMPR")){
            resourceManager.setRegister(9,resourceManager.getRegister(r1)-resourceManager.getRegister(r2)); // SW레지스터에 (r1) : (r2) CC 저장
        }else if(name.equals("TIXR")){
            int xValue1 = resourceManager.getRegister(1);
            resourceManager.setRegister(1, xValue1 + 1);
            int xValue2 = resourceManager.getRegister(r1);
            int xValue3 = resourceManager.getRegister(1);
            resourceManager.setRegister(9, xValue3 - xValue2);
        }
    }
    /*device관련 처리*/
    public void executeDevice(String name,ResourceManager resourceManager) throws Exception {
        if(name.equals("TD")){
            if(VisualSimulator.chosenFile!=null){
                resourceManager.setRegister(9, 1); // SW레지스터(CC)를 0이 아닌 값으로 저장
                return;
            }else{
                throw new Exception("선택된 파일이 없습니다.");
            }
        }else if(name.equals("RD")){
            char readChar = (char)fileReader.read(); // 기존 파일에서 읽기
            if (readChar == '\uFFFF') {
                readChar=0;
            }
            resourceManager.setRegister(0,readChar); // A 레지스터에 읽어온 파일 내용 저장
        }else if(name.equals("WD")) {
            byte writeChar = (byte) resourceManager.getRegister(0); // A 레지스터에 저장한 내용 가져오기
            fileWriter.write(writeChar); // 새로운 파일에 쓰기
        }
    }
    /* register 문자에 해당하는 번호 리턴하는 함수 */
    public int getRegisterNumber(char register) {
        switch (register) {
            case 'A':
                return 0;
            case 'X':
                return 1;
            case 'L':
                return 2;
            case 'B':
                return 3;
            case 'S':
                return 4;
            case 'T':
                return 5;
            case 'F':
                return 6;
            default:
                throw new IllegalArgumentException("존재하지 않는 레지스터입니다.");
        }
    }
}
