import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.io.FileNotFoundException;
import java.io.IOException;

public class InstructionTable {
	/**
	 * 기계어 목록 파일을 읽어, 기계어 목록 테이블을 초기화한다.
	 * 
	 * @param instFileName 기계어 목록이 적힌 파일
	 * @throws FileNotFoundException 기계어 목록 파일 미존재
	 * @throws IOException           파일 읽기 실패
	 */
	/** 기계어 목록 테이블. key: 기계어 명칭, value: 기계어 정보 */
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
		// TODO: fileName의 파일을 열고, 해당 내용을 파싱하여 instructionMap에 저장하기.
	}

	/**
	 * 기계어 목록 테이블에서 특정 기계어를 검색한다.
	 * 
	 * @param instructionName 검색할 기계어 명칭
	 * @return 기계어 정보. 없을 경우 empty
	 */
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

	public HashMap<String, InstructionInfo> getInstructionMap() {
		return instructionMap;
	}

}