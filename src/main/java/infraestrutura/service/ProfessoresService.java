package infraestrutura.service;

import infraestrutura.dao.ProfessoresDao;
import infraestrutura.exception.ErrorMessage;
import infraestrutura.exception.NegocioException;
import infraestrutura.model.Professor;
import infraestrutura.util.CadernetaUtil;
import infraestrutura.util.MessageResourcesConstants;

import org.hsqldb.lib.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProfessoresService extends GenericPojoService<Professor> {

	@Autowired
	private ProfessoresDao professoresDao;
	
	@Override
	public void salvar(Professor professor) {
		NegocioException exception = new NegocioException();
		
		if(StringUtil.isEmpty(professor.getMatricula())){
			exception.addMessage(ErrorMessage.ERRO_CAMPO_OBRIGATORIO, MessageResourcesConstants.MATRICULA);
		}
		
		if(StringUtil.isEmpty(professor.getNome())){
			exception.addMessage(ErrorMessage.ERRO_CAMPO_OBRIGATORIO, MessageResourcesConstants.NOME);
		}
		
		if(StringUtil.isEmpty(professor.getEmail())){
			exception.addMessage(ErrorMessage.ERRO_CAMPO_OBRIGATORIO, MessageResourcesConstants.EMAIL);
		}else if(!CadernetaUtil.validarEmail(professor.getEmail())){
			exception.addMessage(ErrorMessage.ERRO_EMAIL_INVALIDO,professor.getEmail());
		}
		
		if(exception.hasException()){
			throw exception;
		}
		
		super.salvar(professor);
	}
	
	public ProfessoresDao getDAO() {
		return professoresDao;
	}

	public void setProfessoresDao(ProfessoresDao professoresDao) {
		this.professoresDao = professoresDao;
	}

	public ProfessoresDao getProfessoresDao() {
		return professoresDao;
	}
	
}
