import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.io.FileNotFoundException;
import java.io.IOException;

public class InstructionTable {
    private HashMap<String, InstructionInfo> instructionMap;
    public InstructionTable(String instFileName) throws FileNotFoundException, IOException {
        instructionMap = new HashMap<String, InstructionInfo>();
        try (BufferedReader br = new BufferedReader(new FileReader(instFileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                InstructionInfo instructionInfo = new InstructionInfo(line);
                instructionMap.put(instructionInfo.getName(), instructionInfo);
            }
        }catch (IOException e) {
            throw new IOException("Fail to read file : " + e.getMessage());
        }
    }
    public Optional<InstructionInfo> search(String instructionName) {
        if (instructionName == null || instructionName.isEmpty()) {
            return Optional.empty();
        }
        InstructionInfo instructionInfo = instructionMap.get(instructionName);
        // instructionName에 '+'가 포함된 경우 처리
        if (instructionInfo == null && instructionName.charAt(0) == '+') {
            // '+'를 제외하고 다시 검색
            instructionInfo = instructionMap.get(instructionName.substring(1));
        }
        return Optional.ofNullable(instructionInfo);
    }
    public Optional<InstructionInfo> searchOpcode(int opcode) {
        for (InstructionInfo instructionInfo : instructionMap.values()) {
            if (instructionInfo.getOpcode() == opcode) {
                return Optional.of(instructionInfo);
            }
        }
        return Optional.empty();
    }
    public HashMap<String, InstructionInfo> getInstructionMap() {
        return instructionMap;
    }

}