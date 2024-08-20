import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Optional;

public class ControlSection {
	/**
	 * pass1 작업을 수행한다. 기계어 목록 테이블을 통해 소스 코드를 토큰화하고, 심볼 테이블 및 리터럴 테이블을 초기화환다.
	 * 
	 * @param instTable 기계어 목록 테이블
	 * @param input     하나의 control section에 속하는 소스 코드. 마지막 줄은 END directive를 강제로
	 *                  추가하였음.
	 * @throws RuntimeException 소스 코드 컴파일 오류
	 */
	public ControlSection(InstructionTable instTable, ArrayList<String> input) throws RuntimeException {

		_instTable = instTable;
		_tokens = new ArrayList<Token>();
		_symbolTable = new SymbolTable();
		_literalTable = new LiteralTable();

		// TODO: pass1 수행하기.'
		int locctr = 0;
		String controlSectionName = input.get(0).split("\\s+")[0];
		for(String line : input) {
			// System.out.println(line);
			Token token = new Token(line);
			_tokens.add(token);
			// input line을 token으로 쪼개기
			if(token.getOperator().isEmpty())
				continue;
			// 주석인 경우 넘기기
			nixbpe(token);
			// nixbpe 설정

			String label = token.getLabel().orElse("");
			String operator = token.getOperator().orElse("");
			ArrayList<String> operands = token.getOperands();
			String first_operand = operands.size()>0 ? operands.get(0) : "" ;
			String controlSection = controlSectionName;

			if(operator.equals("START")||operator.equals("EXTREF")||operator.equals("CSET")){
				controlSection = "";
			}else{
				controlSection = controlSectionName;
			}

			String finalControlSectionName = controlSection;

			if (!label.isEmpty()){
				try{
                	if (operator.equals("EQU")) {
                    	String equation = first_operand;
                    	_symbolTable.putLabel(label, locctr, equation, finalControlSectionName);
                	} else if(operator.equals("CSECT")){
						_symbolTable.putLabel(label, locctr);
					} else {
                    _symbolTable.putLabel(label, locctr, finalControlSectionName);
                	}
            	} catch (RuntimeException e) {
                throw new RuntimeException("Error in line: " + line + "\n" + e.getMessage());
            	}
			}else if(operator.equals("EXTREF")){
				_symbolTable.putRefer(operands);
			}else if(operator.equals("END")){
				_symbolTable.linkwithRefer();
			}

			if(!first_operand.isEmpty() && first_operand.charAt(0)=='='){
				_literalTable.putLiteral(first_operand);
			} else if (operator.equals("END") || operator.equals("LTORG")) {
				locctr = _literalTable.putAddress(locctr);
				_cs_len = locctr;
				continue;
			}

			// locctr 계산
			locctr = compute_locctr(locctr,operator,operands);
		}
	}
	public int compute_locctr(int locctr,String token_operator,ArrayList<String> operands){

		int token_format = find_format(token_operator);
		if (!token_operator.isEmpty()&&token_operator.charAt(0)=='+'){
			token_format = 4 ;
		}
		switch(token_format){
			case 0:
				String first_operand = operands.size()>0 ? operands.get(0) : "" ;
				if(token_operator.equals("WORD")){
					locctr += 3;
				}else if(token_operator.equals("BYTE")){
					int length = lengthInQuotes(first_operand);
					if(first_operand.charAt(0)=='X'){
						length = length/2;
					}
					locctr += length;
				}else if(token_operator.equals("RESW")){
					locctr += 3 * Integer.parseInt(first_operand);
				}else if(token_operator.equals("RESB")){
  					locctr += Integer.parseInt(first_operand);
				}else{
					locctr += 0;
				}
				break;
			case 2:
				locctr += token_format;
				break;
			case 3:
				locctr += token_format;
				break;
			case 4:
				locctr += 4;
				break;
		}
		return locctr;
	}
	public int find_format(String token_operator){
		Optional<InstructionInfo> instructionInfoOptional = _instTable.search(token_operator);
		return instructionInfoOptional.get().getFormat();
	}
	public static int lengthInQuotes(String operandStr) {
		int length = 0;
		boolean inQuotes = false;
		for (int i = 0; i < operandStr.length(); i++) {
			char ch = operandStr.charAt(i);
			if (ch == '\'') {
				inQuotes = !inQuotes;
			} else if (inQuotes) {
				length++;
			}
		}
		return length;
	}
	public void nixbpe(Token token) throws RuntimeException{
		String token_operator = String.valueOf(token.getOperator().orElse(""));
		ArrayList<String> token_operands = token.getOperands();
		int token_format = find_format(token_operator);

		if (!token_operator.isEmpty()&&token_operator.charAt(0)=='+'){
			token_format = 4 ;
		}

        switch (token_format) {
            case 4 -> {
                if (token_operands.size() > 1 && token_operands.get(1).equals("X")) {
                    // operand에 최소 하나 이상의 요소가 있고
                    token.setNixbpeBinary("111001");
                } else {
                    token.setNixbpeBinary("110001");
                }
            }
            case 3 -> {
				if(!token_operands.isEmpty() && token_operands.get(0).charAt(0) == '='){
					token.setNixbpeBinary("110010");
				}
                else if (!token_operands.isEmpty() && token_operands.get(0).charAt(0) == '#') {
                    if (Character.isDigit(token_operands.get(0).charAt(1))) {
                        token.setNixbpeBinary("010000");
                    } else {
                        token.setNixbpeBinary("010010");
                    }
                }else if(token_operator.equals("RSUB")){
					token.setNixbpeBinary("110000");
				}else if (!token_operands.isEmpty() && token_operands.get(0).charAt(0) == '@') {
                    token.setNixbpeBinary("100010");
                } else if (!Character.isDigit(token_operands.get(0).charAt(0))) {
                    if (token_operands.size()>1 && token_operands.get(1).charAt(0) == 'X') {
                        token.setNixbpeBinary("111010");
                    } else {
                        token.setNixbpeBinary("110010");
                    }
                }
            }
			case 2->{
				token.setNixbpeBinary("0000010");
			}
        }
	}
	/**
	 * pass2 작업을 수행한다. pass1에서 초기화한 토큰 테이블, 심볼 테이블 및 리터럴 테이블을 통해 오브젝트 코드를 생성한다.
	 *
	 * @return 해당 control section에 해당하는 오브젝트 코드 객체
	 * @throws RuntimeException 소스 코드 컴파일 오류
	 */
	public ObjectCode buildObjectCode() throws RuntimeException {
		ObjectCode objCode = new ObjectCode();
		int locctr = 0;
		int pc = 0;
		// TODO: pass2 수행하기.
		for(Token token : _tokens){
			String operator = token.getOperator().orElse("");

			// 주석일 경우 넘기기
			if(operator.isEmpty()){
				continue;
			}

			ArrayList<String> operands = token.getOperands();
			pc =  compute_locctr(locctr,operator,operands);

			if(operator.equals("START")||operator.equals("CSECT")){
				boolean program = false;
				if(operator.equals("START")){
					program = true;
				}
				objCode.makeHeader(token.getLabel().orElse(""),locctr,_cs_len,program);
			}else if(operator.equals("EXTDEF")) {
				objCode.makeDefiner(token,_symbolTable);
			} else if(operator.equals("EXTREF")){
				objCode.makeRefer(token.getOperands());
			}
			else{
				objCode.addText(locctr,pc,token,_symbolTable,_instTable,_literalTable);
			}

			locctr = compute_locctr(locctr,operator,operands);
		}
		return objCode;
	}

	/**
	 * 심볼 테이블을 String으로 변환하여 반환한다. Assembler.java에서 심볼 테이블을 출력하는 데에 사용된다.
	 * 
	 * @return 문자열로 변경된 심볼 테이블
	 */
	public String getSymbolString() {
		return _symbolTable.toString();
	}

	/**
	 * 리터럴 테이블을 String으로 변환하여 반환한다. Assembler.java에서 리터럴 테이블을 출력하는 데에 사용된다.
	 * 
	 * @return 문자열로 변경된 리터럴 테이블
	 */
	public String getLiteralString() {
		return _literalTable.toString();
	}

	/** 기계어 목록 테이블 */
	private InstructionTable _instTable;

	/** 토큰 테이블 */
	private ArrayList<Token> _tokens;

	/** 심볼 테이블 */
	private SymbolTable _symbolTable;

	/** 리터럴 테이블 */
	private LiteralTable _literalTable;

	/** 프로그램 길이(control_section length)*/
	private int _cs_len;
}