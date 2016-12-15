package infraestrutura.bean;

import infraestrutura.bean.enumeration.Operacao;
import infraestrutura.bean.infra.SelecaoPojo;
import infraestrutura.bean.navigation.AliasNavigation;
import infraestrutura.exception.InfoMessage;
import infraestrutura.model.Professor;
import infraestrutura.util.ManagedBeanUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.event.AjaxBehaviorEvent;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("professoresMB")
@Scope(GenericMB.SESSAO)
public class ProfessoresMB extends GenericMB {

	private static final long serialVersionUID = 1L;
	
	private List<Professor> professores;
	private SelecaoPojo<Professor> professoresSelecionados;
	
	private Professor professor;

	public ProfessoresMB(){
		professores = new ArrayList<Professor>();
		professoresSelecionados = new SelecaoPojo<Professor>();
	}
	
	public String inicializar() {
		professores = professoresService.listar();
		operacao = Operacao.LISTAR;
		professoresSelecionados.clear();
		selecionarTodos = false;
		return AliasNavigation.PROFESSORES;
	}
	
	public void selecionarTodos(AjaxBehaviorEvent event){
		if(selecionarTodos){
			for(Professor p : professores){
				professoresSelecionados.put(p, "true");
			}
		}else{
			professoresSelecionados.clear();
		}
	}

	public List<Professor> getProfessores() {
		return professores;
	}

	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
	}

	@Override
	public void prepararInclusao() {
		professor = new Professor();
	}
	
	public void salvar(){
		professoresService.salvar(professor);
		inicializar();
		ManagedBeanUtil.adicionarMensagem(InfoMessage.OPERACAO_SUCESSO.getMensagem(), FacesMessage.SEVERITY_INFO);
	}
	
	public void excluir(){
		professoresService.excluir(professoresSelecionados.keySetSelected());
		inicializar();
		ManagedBeanUtil.adicionarMensagem(InfoMessage.OPERACAO_SUCESSO.getMensagem(), FacesMessage.SEVERITY_INFO);
	}

	public SelecaoPojo<Professor> getProfessoresSelecionados() {
		return professoresSelecionados;
	}

	public void setProfessoresSelecionados(
			SelecaoPojo<Professor> professoresSelecionados) {
		this.professoresSelecionados = professoresSelecionados;
	}
	
	public boolean isPodeExcluir(){
		return !professoresSelecionados.keySetSelected().isEmpty() && isOperacaoListar();
	}

	public List<String> getTitulacao() {
		return Arrays.asList("ESPECIALISTA", "MESTRE", "DOUTOR");
	}
}
