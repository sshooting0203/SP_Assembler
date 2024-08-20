import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectCodeLoader {
    private String programeName;
    private String startAddress;
    private String programLen;

    public String getExecuteAddress() {
        return executeAddress;
    }

    private String executeAddress;
    public String getProgrameName() {
        return programeName;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public String getProgramLen() {
        return programLen;
    }
    private HashMap<String, Integer> ESTB = new HashMap<String, Integer>();
    public int loadObjectCode(String objectCode, ResourceManager resourceManager) throws Exception {
        this.pass1(objectCode);
        return this.pass2(objectCode,resourceManager);
    }
    public void pass1(String objectCode) throws Exception {
        List<String> control_sections = splitInputByEmptyLine(objectCode); // objectcode를 control_section으로 분리

        int CSLTH = 0; // control_section의 길이
        int CSADDR = 0; // 각 control_section의 시작 주소
        boolean firstProcess=false;

        for (String control_section : control_sections) { // 하나의 control_section씩 진행
            String record[] = control_section.split("\\n+");
            char firstRecordType = record[0].trim().charAt(0);
            if (firstRecordType == 'H') {
                String[] part = record[0].split("\\s+"); // 한 줄을 공백을 기준으로 parsing

                String control_name = extractCurrentSection(record[0].trim().substring(1));
                // int control_address = Integer.parseInt(part[1].substring(0, 6), 16);
                CSLTH = Integer.parseInt(part[1].substring(6), 16);

                if (ESTB.containsKey(control_name)) {
                    throw new Exception(control_name + "이 이미 ESTB에 존재합니다.");
                } else {
                    ESTB.put(control_name, CSADDR);
                }
                if(!firstProcess){
                    programeName = control_name;
                    programLen =  String.format("%06x",CSLTH);
                    startAddress =  String.format("%06x",CSADDR);
                    firstProcess=true;
                }
            }
            for (int i = 1; i < record.length; i++) {
                char recordType = record[i].trim().charAt(0);
                if (recordType == 'D') {
                    String define_record = record[i].substring(1);
                    Pattern pattern = Pattern.compile("([A-Z]+)(\\p{XDigit}{6})");
                    Matcher matcher = pattern.matcher(define_record);

                    while (matcher.find()) {
                        String letters = matcher.group(1);
                        int indicatedADDR = Integer.parseInt(matcher.group(2),16);
                        if (ESTB.containsKey(letters)) {
                            throw new Exception(letters + "이 이미 ESTB에 존재합니다.");
                        } else {
                            ESTB.put(letters, CSADDR+indicatedADDR);
                        }
                    }
                }
            }
            CSADDR += CSLTH;
        }
    }
    public int pass2(String objectCode, ResourceManager resourceManager) throws Exception {
        int CSLTH = 0; // control_section의 길이
        int CSADDR = 0; // 각 control_section의 시작 주소
        int EXECADDR = 0;
        List<String> control_sections = splitInputByEmptyLine(objectCode); // objectcode를 control_section으로 분리

        for (String control_section : control_sections) {
            String record[] = control_section.split("\\n+");
            char firstRecordType = record[0].trim().charAt(0);
            if (firstRecordType == 'H') {
                String[] part = record[0].split("\\s+"); // 한 줄을 공백을 기준으로 parsing
                CSLTH = Integer.parseInt(part[1].substring(6), 16);
            }
            for (int i = 1; i < record.length; i++) {
                char recordType = record[i].trim().charAt(0);
                if (recordType == 'T') {
                    int text_start = Integer.parseInt(record[i].substring(1,7),16);
                    // int text_length = Integer.parseInt(record[i].substring(7,9),16);
                    List<Byte> packed_bytes = pack(record[i].substring(9));
                    for (int j = 0; j < packed_bytes.size(); j++) {
                        int address = CSADDR + text_start + j;
                        byte value = packed_bytes.get(j);
                        resourceManager.setMemory(address, value);
                    }
                }else if(recordType == 'M'){
                    int modify_start = Integer.parseInt(record[i].substring(1,7),16);
                    int modify_length = Integer.parseInt(record[i].substring(7,9),16);
                    char operator = record[i].charAt(9);
                    String modify_symbol = record[i].substring(10,record[i].length()).strip();
                    if(modify_length%2!=0){ //짝수 길이
                        modify_start+=1;
                    }
                    modify_length /=2;
                    if(ESTB.containsKey(modify_symbol)){
                        int symbol_value = ESTB.get(modify_symbol);
                        modifyMemory(CSADDR+modify_start,modify_length,symbol_value,operator,resourceManager);
                    }else{
                        throw new Exception(modify_symbol + "이 ESTB에 존재하지 않습니다.");
                    }
                }
            }
            if(record[record.length-1].charAt(0) == 'E'){
                String end_record = record[record.length-1];
                if(end_record.length()>1){
                    EXECADDR = CSADDR + Integer.parseInt(end_record.substring(1), 16);
                    executeAddress = String.format("%06x",EXECADDR);
                }
            }
            CSADDR += CSLTH;
        }
        return EXECADDR;
        // jump to location given by EXECADDR(to start executio of loaded program
    }
    public List<Byte> pack(String text_record){
        int text_length = text_record.length();
        List<Byte> text_byte = new ArrayList<>();
        for(int i=0;i<text_length;i+=2){
            String hex_pair = text_record.substring(i,i+2);
            byte text_packed = (byte) Integer.parseInt(hex_pair,16);
            text_byte.add(text_packed);
        }
        return text_byte;
    }
    public void modifyMemory(int address, int length, int value, char operator,ResourceManager resourceManager) throws Exception {
        int originalValue = 0;
        for (int i = 0; i < length; i++) {
            originalValue = (originalValue << 8) | (resourceManager.getMemory(address + i) & 0xFF);
        }
        int modifiedValue;
        if(operator == '+'){
            modifiedValue = originalValue + value;
        }else if(operator == '-'){
            modifiedValue = originalValue - value;
        }else{
            throw new Exception("+/- 연산자만 허용합니다.");
        }
        for (int i = length - 1; i >= 0; i--) {
            resourceManager.setMemory(address + i, (byte) (modifiedValue & 0xFF));
            modifiedValue >>= 8;
        }
    }
    private String extractCurrentSection(String str) {
        StringBuilder section = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                break;
            }
            section.append(c);
        }
        return section.toString().trim();
    }
    public static List<String> splitInputByEmptyLine(String input) {
        List<String> segments = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(input));
        StringBuilder currentSegment = new StringBuilder();

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    if (currentSegment.length() > 0) {
                        segments.add(currentSegment.toString().trim());
                        currentSegment.setLength(0); // Clear the StringBuilder
                    }
                } else {
                    currentSegment.append(line).append("\n");
                }
            }
            // 마지막 세그먼트 추가
            if (currentSegment.length() > 0) {
                segments.add(currentSegment.toString().trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return segments;
    }
}
