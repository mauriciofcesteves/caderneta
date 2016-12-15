package infraestrutura.service;

import infraestrutura.bean.dto.DashboardDTO;
import infraestrutura.bean.dto.JobDTO;
import infraestrutura.bean.dto.JobsAgrupadosPorCoordenadorDTO;
import infraestrutura.dao.JobsDao;
import infraestrutura.exception.ErrorMessage;
import infraestrutura.exception.NegocioException;
import infraestrutura.model.Coordenador;
import infraestrutura.model.Job;
import infraestrutura.model.ParametroSistema;
import infraestrutura.util.EmailUtil;
import infraestrutura.util.ManagedBeanUtil;
import infraestrutura.util.MessageResourcesConstants;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.hsqldb.lib.StringUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobsService extends GenericPojoService<Job> {

	@Autowired
	private JobsDao jobsDao;

	@Autowired
	private CoordenadoresService coordenadoresService;

	@Autowired
	private ParametroSistemaService parametroSistemaService;

	public void salvar(Job job, List<Coordenador> coordenadores) {
		NegocioException exception = new NegocioException();

		if(StringUtil.isEmpty(job.getTitulo())){
			exception.addMessage(ErrorMessage.ERRO_CAMPO_OBRIGATORIO, MessageResourcesConstants.TITULO);
		}

		if(StringUtil.isEmpty(job.getDescricao())){
			exception.addMessage(ErrorMessage.ERRO_CAMPO_OBRIGATORIO, MessageResourcesConstants.DESCRICAO);
		}

		if(job.getDataPrevista() == null){
			exception.addMessage(ErrorMessage.ERRO_CAMPO_OBRIGATORIO, MessageResourcesConstants.DATA_PREVISTA);
		}

		if (coordenadores.isEmpty()) {
			exception.addMessage(ErrorMessage.ERRO_CAMPO_OBRIGATORIO, MessageResourcesConstants.COORDENADORES);
		}
		
		if(job.getDataPrevista() != null && job.getDataPrevista().isBefore(new LocalDate())){
			exception.addMessage(ErrorMessage.ERRO_DATA_MENOR_ATUAL, MessageResourcesConstants.DATA_PREVISTA);
		}

		if(exception.hasException()){
			throw exception;
		}

		job.setDataCadastro(new LocalDate());

		List<Job> jobs = new ArrayList<Job>();
		if (job.getCoordenador() == null) {
			criarInstanciasDeJobParaTodosCoordenadores(jobs, job, coordenadores);
		} else {
			jobs.add(job);
		}

		for (Job j : jobs) {
			super.salvar(j);

			//enviar e-mail para o coordenador informando atividade atribuida
			ParametroSistema parametro = parametroSistemaService.getParametroSistema();
			EmailUtil.enviarEmailTratandoErro(parametro, "NOVO JOB: "+j.getTitulo(), "Descrição do Job: "+j.getDescricao()+" Data Prevista de conclusão: "+j.getDataPrevista().toString("dd/MM/yyyy"), j.getCoordenador().getEmail());
		}
	}

	public void alterar(Job job) {
		super.salvar(job);
	}

	public void criarInstanciasDeJobParaTodosCoordenadores(List<Job> jobs, Job job, List<Coordenador> coordenadores) {
		for (Coordenador coordenador : coordenadores) {
			Job j = new Job();
			j.setCoordenador(coordenador);
			j.setDataCadastro(new LocalDate());
			j.setDataPrevista(job.getDataPrevista());
			j.setDescricao(job.getDescricao());
			j.setTitulo(job.getTitulo());
			jobs.add(j);
		}
	}

	public JobsDao getDAO() {
		return jobsDao;
	}

	public void setJobsDao(JobsDao jobsDao) {
		this.jobsDao = jobsDao;
	}

	public JobsDao getJobsDao() {
		return jobsDao;
	}

	public List<JobsAgrupadosPorCoordenadorDTO> listaJobsAgrupadosPorCoordenador(HashMap<String, Object> filtro) {
		List<Job> jobs = jobsDao.consultarParaAgrupamento(filtro);
		List<JobsAgrupadosPorCoordenadorDTO> retorno = new ArrayList<JobsAgrupadosPorCoordenadorDTO>();
		JobsAgrupadosPorCoordenadorDTO agrupados = null;
		JobDTO dto = null;

		Integer idCoordenadorAnterior = null;
		for (Job job : jobs) {

			if (idCoordenadorAnterior == null || !idCoordenadorAnterior.equals(job.getCoordenador().getId())) {
				agrupados = new JobsAgrupadosPorCoordenadorDTO();
				agrupados.setNomeCoordenador(job.getCoordenador().getNome());
				agrupados.setCoordenador(job.getCoordenador());
				retorno.add(agrupados);
			}

			dto = new JobDTO();
			dto.setJob(job);
			agrupados.adicionarJob(dto);
			idCoordenadorAnterior = job.getCoordenador().getId();
		}

		return retorno;
	}

	@SuppressWarnings("unchecked")
	public List<DashboardDTO> getDashboard(List<Coordenador> coordenadores, LocalDate dataInicioCadastro, LocalDate dataFimCadastro){
		if(dataInicioCadastro != null && dataFimCadastro != null && dataFimCadastro.isBefore(dataInicioCadastro)){
			throw new NegocioException(ErrorMessage.DATA_FINAL_MENOR_INICIAL, MessageResourcesConstants.ATE, MessageResourcesConstants.DATA_CADASTRO);
		}
		
		List<DashboardDTO> dashBoard = new ArrayList<DashboardDTO>();

		DashboardDTO dto;
		int qtdeJobs;
		int qtdeJobsConcluidos;
		int qtdeJobsConcluidosComAtraso;
		int qtdeJobsNaoConcluidos;

		Collections.sort(coordenadores,new BeanComparator("nome"));
		List<Coordenador> coordenadoresRelatorio = coordenadores;
		if(coordenadoresRelatorio.isEmpty()){
			coordenadoresRelatorio = coordenadoresService.listar();
		}

		for(Coordenador coordenador : coordenadoresRelatorio){
			dto = new DashboardDTO();
			dto.setCoordenador(coordenador.getNome());
			qtdeJobs = 0;
			qtdeJobsConcluidos = 0;
			qtdeJobsConcluidosComAtraso = 0;
			qtdeJobsNaoConcluidos = 0;
			for(Job job : jobsDao.consultarParaRelatorio(coordenador, dataInicioCadastro, dataFimCadastro)){
				qtdeJobs++;
				if(job.getDataConclusao() != null){
					if(job.getDataConclusao().isAfter(job.getDataPrevista())){
						qtdeJobsConcluidosComAtraso++;
					}else{
						qtdeJobsConcluidos++;
					}
				}else{
					qtdeJobsNaoConcluidos++;
				}
			}

			dto.setQtdeJobs(qtdeJobs);
			dto.setQtdeJobsConcluidos(qtdeJobsConcluidos);
			dto.setQtdeJobsConcluidosComAtraso(qtdeJobsConcluidosComAtraso);
			dto.setQtdeJobsNaoConcluidos(qtdeJobsNaoConcluidos);

			JFreeChart chart = getGrafico(dto);
			dto.setImage(chart.createBufferedImage(1000, 500));
			dashBoard.add(dto);
		}

		return dashBoard;
	}

	public JFreeChart getGrafico(DashboardDTO dashBoard){
		JFreeChart chart = ChartFactory.createBarChart("Jobs", "Situação", "%", createDataset(dashBoard), PlotOrientation.HORIZONTAL, true, true, false);
		chart.setBackgroundPaint(Color.WHITE);
		
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.BLACK);
		
		final CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setVisible(false);

		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setRange(0.0, 100.0);
		rangeAxis.setVisible(true);

		plot.getRenderer().setBasePaint(Color.WHITE);
		plot.getRenderer().setSeriesPaint(0, Color.GREEN);
		plot.getRenderer().setSeriesPaint(1, Color.YELLOW);
		plot.getRenderer().setSeriesPaint(2, Color.RED);

		return chart;
	}

	private CategoryDataset createDataset(DashboardDTO dashBoard) {
		String concluidoPrazo = ManagedBeanUtil.getMessage("concluido_prazo");
		String concluidoForaPrazo = ManagedBeanUtil.getMessage("concluido_fora_prazo");
		String naoConcluido = ManagedBeanUtil.getMessage("nao_concluido");

		String nome = dashBoard.getCoordenador();

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		dataset.addValue(dashBoard.getPercentualConcluido(), concluidoPrazo, nome);
		dataset.addValue(dashBoard.getPercentualConcluidoComAtraso(), concluidoForaPrazo, nome);
		dataset.addValue(dashBoard.getPercentualNaoConcluido(), naoConcluido, nome);

		//		DatasetUtilities.createCategoryDataset("Situação ", "Coordenador ", new double[][] {new double[]{dashBoard.getPercentualConcluido()}, new double[]{dashBoard.getPercentualConcluidoComAtraso()},new double[]{dashBoard.getQtdeJobsNaoConcluidos()}});

		return dataset;
	}


}

