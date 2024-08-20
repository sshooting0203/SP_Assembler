import java.util.ArrayList;
import java.util.Optional;

public class ObjectCode {
	public ObjectCode() {
		// TODO: 초기화.
		textRecords = new ArrayList<>();
		modifyRecord = new ArrayList<>();
		textRecords_len = 0;
		origin_start = 0;
		current_locctr = 0;
		ltorg_locctr = 0;
		endFlag = false;
		firstText = "";
		lastText = "";
	}
	public void makeHeader(String label, int locctr, int program_len,boolean program){
		headerRecord = "H"+label+ "\t" + String.format("%06X", locctr) + String.format("%06X", program_len);
		endFlag = program;
	}

	public void makeDefiner(Token token,SymbolTable _symbolTable){
		String define_str ="";
		String operator = token.getOperator().orElse("");
		for(String operand : token.getOperands()){
			int add = _symbolTable.getAddress(operand).orElse(0);
			define_str = define_str + operand + String.format("%06X", add);
		}
		defineRecord = "D"+ define_str;
	}
	public void makeRefer(ArrayList<String> operands){
		referenceRecord = "R";
		for(String oper : operands){
			referenceRecord = referenceRecord+oper+" ";
		}
		refer_str = operands;
	}
	public void makeText(int locctr){
		origin_start = locctr;
		firstText = "T"+String.format("%06X", locctr);
		textRecords_len += firstText.length()+2;
	}
	public void makeModify(int calculated_locctr,String ref_str,int len){
		String[] parts = ref_str.split("[+-]");
		for(String part : parts){
			if (ref_str.indexOf(part) > 0 && ref_str.charAt(ref_str.indexOf(part) - 1) == '-') {
				modifyRecord.add("M" + String.format("%06X",calculated_locctr) + String.format("%02X",len) + "-" + part);
			} else {
				modifyRecord.add("M" + String.format("%06X",calculated_locctr) + String.format("%02X",len) + "+" + part);
			}
		}
	}
	public void makeEnd(){
		endRecord = "E";
		if(endFlag){
			endRecord = endRecord + "0".repeat(6);
		}
	}
	/*
	firstText에 추가할 objCode 길이 2글자를 추가하는 메서드
	 */
	public void add_len(int locctr){
		firstText = firstText + String.format("%02X", locctr-origin_start);
	}
	/*
	레지스터 문자열에 맞는 숫자(number)를 리턴하는 메서드
	 */
	public int matchReg(String reg_str){
		Character reg = reg_str.charAt(0);
		switch (reg){
			case 'A':
				return 0;
			case 'X':
				return 1;
			case 'S':
				return 4;
			case 'T':
				return 5;
		}
		return -1;
	}
	public void addText(int locctr, int pc,Token token, SymbolTable symbolTable,InstructionTable instTable,LiteralTable literalTable){
		int add_len = 0;
		String res="";
		String operator = token.getOperator().orElse("");
		ArrayList<String> operands = token.getOperands();

		if(!operator.isEmpty()){
			if(operator.equals("EQU")){
				return;
			}
			else if(operator.equals("RESW")||operator.equals("RESB")){
				data_count++;
			}
			else if(operator.equals("WORD")||operator.equals("BYTE")){
				String first_operand = operands.size()>0 ? operands.get(0) : "" ;
				res = compute_value(first_operand,locctr);
				add_len = res.length();
				current_locctr = pc;
			}
			else if(operator.equals("LTORG")||operator.equals("END")){
				if(operator.equals("END")){
					if(ltorg_locctr==0 && !literalTable.isLiteralMapEmpty()){
						res = literalTable.gatherValue(ltorg_locctr,locctr);
						add_len = res.length();
						current_locctr += add_len / 2;
					}
					makeEnd();
				}else{
					if(ltorg_locctr==0 && !literalTable.isLiteralMapEmpty()){
						res = literalTable.gatherValue(ltorg_locctr,locctr);
						if(!res.isEmpty()){
							current_locctr = locctr + res.length()/2;
						}
						add_len = res.length();
						ltorg_locctr = locctr;
						makeText(locctr);
						add_len(current_locctr);
						lastText = res;
						if(!lastText.isEmpty()){
							textRecords.add(firstText + lastText);
						}
						setFields();
						return;
					}
				}
			}
			else{
				Optional<InstructionInfo> instructionInfoOptional = instTable.search(operator);
				int opcode = instructionInfoOptional.get().getOpcode();
				int nixbpe_bit = token.getNixbpe().orElse(-1);
				if(nixbpe_bit == 2){
					res = String.format("%02X",opcode);
					for(String oper : operands){
						int reg_num = matchReg(oper);
						res += String.format("%01X",reg_num);
					}
					if(operands.size()==1){
						res += "0";
					}
					current_locctr = pc;
					add_len = res.length();
				}
				else if(operator.equals("RSUB")){
					String nixbpe_str = Integer.toBinaryString(nixbpe_bit);
					res = convertOpcode(opcode) +String.format("%06d", Integer.parseInt(nixbpe_str));
					res += "0".repeat(12);
					res = Integer.toHexString(Integer.parseInt(res, 2)).toUpperCase();
					current_locctr = pc;
					add_len = res.length();
				}
				else{
					String nixbpe_str = Integer.toBinaryString(nixbpe_bit);
					res = convertOpcode(opcode) +String.format("%06d", Integer.parseInt(nixbpe_str));
					res += remainBits(operator,operands,locctr,pc,symbolTable,literalTable);
					res = Integer.toHexString(Integer.parseInt(res, 2)).toUpperCase();
					if(operator.charAt(0)=='+'){
						res = resizeDigits(8,res);
					}else{
						res = resizeDigits(6,res);
					}
					if(instructionInfoOptional.get().getFormat()!=0){
						current_locctr = pc;
					}
					add_len = res.length();
				}
			}
		}

		// RESB,RESW일 때는 objcode 생성 x
		if(textRecords_len==0 && !operator.equals("RESB") && !operator.equals("RESW")){
			makeText(locctr);
		}
		// END에 도달 or 한 줄 최대길이 초과한 경우(미리 계산한 값) or data영역으로 진입한 경우
		if(textRecords_len + add_len > 65|| data_count==1||operator.equals("END")) {
			add_len(current_locctr);
			// 먼저 길이 2글자를 추가하고
			if(!lastText.isEmpty()){
				textRecords.add(firstText + lastText+res);
				// firstText와 lastText를 합침
			}
			setFields();
			return ;
		}
		lastText += res;
		textRecords_len +=add_len;
	}
	public String convertOpcode(int opcode) {
		// 16진수로 변환
		String hexString = Integer.toHexString(opcode);
		// 2진수로 변환
		String binaryString = Integer.toBinaryString(Integer.parseInt(hexString, 16));
		// 뒤 2개의 비트 제거 후 반환
		while (binaryString.length() < 8) {
			binaryString = "0" + binaryString;
		}
		// 2진수를 8자리의 문자열로 변환하여 반환(앞에 붙이기)
		return binaryString.substring(0, binaryString.length() - 2);
	}
	public String resizeDigits(int len,String hexString) {
		// len : 맞추기 원하는 자리수
		int pad_len = len-hexString.length();
		// 원하는 자리수가 크다 => 0을 추가, 원하는 자리수가 작다 => 잘라내기
		if (pad_len > 0) {
			hexString = "0".repeat(pad_len) + hexString;
		} else if (pad_len < 0) {
			hexString = hexString.substring(hexString.length() - len);
		}
		return hexString;
	}
	public void setFields(){
		textRecords_len = 0;
		lastText = "";
	}
	public String remainBits(String operator, ArrayList<String> operands,int locctr,int pc,SymbolTable symbolTable,LiteralTable literalTable) throws RuntimeException{
		String first_operand = operands.size()>0 ? operands.get(0) : "" ;
		String binaryPC="";

		if(!operands.isEmpty()){
			if(first_operand.charAt(0)=='#'){
				binaryPC = Integer.toBinaryString(Integer.parseInt(first_operand.substring(1)));
			}else if(first_operand.charAt(0)=='@'){

			}else if(first_operand.charAt(0)=='='){
				int add = literalTable.getAddress(first_operand).orElse(-1);
				if(add==-1){
					throw new RuntimeException("Can't find literal in litTab!");
				}else{
					pc = add- pc;
					binaryPC = Integer.toBinaryString(pc);
				}
			}
			else{
				int add = symbolTable.getAddress(first_operand).orElse(-1);
				// jsub 찾을 수 없음, 다른 symtab에서도 찾을 수 없을 때 예외처리 필요함
				if(add==-1){
					int cnt = 0;
					for(String refer:refer_str){
						if(first_operand.contains(refer)) {
							makeModify(locctr+1,first_operand,5);
							break;
						}
						cnt++;
					}
					if(cnt==refer_str.size()){
						throw new RuntimeException("Error : missing symbol definition"+"\n"
								+"(no label)"+"\t"+operator+"\t"+operands);
					}
					pc = 0;
					binaryPC = Integer.toBinaryString(pc);
				}else{
					pc = add- pc;
					binaryPC = Integer.toBinaryString(pc);
				}

			}

			if(operator.charAt(0)=='+'){ // 4형식인 경우 20자리로 리턴
				binaryPC = resizeDigits(20,binaryPC);
			}else{
				binaryPC = resizeDigits(12,binaryPC); // 12자리의 2진수로 표기
			}

			return binaryPC;
		}
		return binaryPC;
	}
	public String compute_value(String operand,int locctr){
		if(operand.contains("\'")){
			int startIndex = operand.indexOf('\'');
			int endIndex = operand.lastIndexOf('\'');
			// 작은 따옴표 사이의 문자 추출
			String new_str = operand.substring(startIndex+1, endIndex);
			if(operand.charAt(0)=='X'){
				return new_str;
			}else if(operand.charAt(0)=='C'){
				StringBuilder asciiStr = new StringBuilder();
				for (char c : new_str.toCharArray()) {
					asciiStr.append(Integer.toHexString((int) c));
				}
				return asciiStr.toString();
			}
		}else if(Character.isDigit(operand.charAt(0))){
			int oper_int = Integer.parseInt(operand);
			return String.format("%06X",oper_int);
		}else{
			for(String refer:refer_str){
				if(operand.contains(refer)) {
					makeModify(locctr,operand,6);
					// EXTREF로 선언한 operands 중 존재함
					break;
				}
			}
			return "0".repeat(6);
		}
		return "";
	}
	/**
	 * ObjectCode 객체를 String으로 변환한다. Assembler.java에서 오브젝트 코드를 출력하는 데에 사용된다.
	 */
	@Override
	public String toString() {
		// TODO: toString 구현하기.
		StringBuilder builder = new StringBuilder();
		builder.append(headerRecord).append("\n");

		// Define Record
		if (defineRecord != null) {
			builder.append(defineRecord).append("\n");
		}

		// Reference Record
		if (referenceRecord != null) {
			builder.append(referenceRecord).append("\n");
		}

		// Text Records
		for (String textRecord : textRecords) {
			builder.append(textRecord).append("\n");
		}

		// Modify Records
		for (String modify : modifyRecord) {
			builder.append(modify).append("\n");
		}

		// End Record
		if (endRecord != null) {
			builder.append(endRecord);
		}
		return builder.toString();
	}

	// TODO: private field 선언.
	private String headerRecord;
	private String defineRecord;
	private String endRecord;
	private Boolean endFlag; // main 프로그램의 endRecord를 작성하는데 사용

	private String referenceRecord;
	private ArrayList<String> refer_str; // EXTREF를 통해 선언된 symbol들
	private ArrayList<String> modifyRecord;

	private ArrayList<String> textRecords;
	private String firstText; // textRecord의 9글자 부분
	private String lastText; // 9글자를 제외한 나머지 부분
	private int textRecords_len; // textRecord 한 줄의 길이

	private int current_locctr;
	private int origin_start; // textRecord의 시작주소
	private int data_count; // data관련 Record가 등장하는 시점
	private int ltorg_locctr; // LTORG : gatherValue의 prev_address 설정

}
