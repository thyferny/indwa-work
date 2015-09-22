package com.alpine.datamining.api.impl.hadoop;




public abstract class AbstractHadoopMRJobAnalyzer extends AbstractHadoopAnalyzer{
	protected AlpineHadoopRunner hadoopRunner =null;
	public AlpineHadoopRunner getHadoopRunner() {
		return hadoopRunner;
	}
	public void setHadoopRunner(AlpineHadoopRunner hadoopRunner) {
		this.hadoopRunner = hadoopRunner;
	}
	public void stop(){
		super.stop();
		if(hadoopRunner!=null){
			hadoopRunner.stop();
		}
	}

}
