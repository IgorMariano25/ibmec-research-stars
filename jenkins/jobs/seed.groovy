// ============================================================
// Job DSL — cria a estrutura de jobs de teste no startup do Jenkins.
// Os jobs são divididos pelas categorias exigidas:
//   01 - Testes de Unidade   (JUnit)
//   02 - Testes de Desempenho (JMeter)
//   03 - Testes Funcionais   (Selenium IDE)
//   00 - Suite Completa      (dispara as 3 acima em sequência)
// ============================================================

def workspacePipelinesDir = new File('/workspace/jenkins/pipelines')
def pipelinesDir = workspacePipelinesDir.exists() ? workspacePipelinesDir.path : '/var/jenkins_conf/pipelines'

folder('research-stars-testes') {
  displayName('IBMEC Research Stars — Testes')
  description('Jobs de teste divididos por categoria: Unidade, Desempenho e Funcional.')
}

pipelineJob('research-stars-testes/00-suite-completa') {
  displayName('00 • Suite Completa (Unidade + Desempenho + Funcional)')
  description('Executa as três categorias de teste em sequência.')
  definition {
    cps {
      sandbox(true)
      script(new File("${pipelinesDir}/suite.Jenkinsfile").text)
    }
  }
}

pipelineJob('research-stars-testes/01-testes-de-unidade') {
  displayName('01 • Testes de Unidade (JUnit)')
  description('Testes unitários (Maven Surefire / JUnit 5, @Tag("unit")).')
  definition {
    cps {
      sandbox(true)
      script(new File("${pipelinesDir}/unit.Jenkinsfile").text)
    }
  }
}

pipelineJob('research-stars-testes/02-testes-de-desempenho') {
  displayName('02 • Testes de Desempenho (JMeter)')
  description('Teste de carga das APIs do backend com Apache JMeter.')
  definition {
    cps {
      sandbox(true)
      script(new File("${pipelinesDir}/performance.Jenkinsfile").text)
    }
  }
}

pipelineJob('research-stars-testes/03-testes-funcionais') {
  displayName('03 • Testes Funcionais (Selenium IDE)')
  description('Testes de front-end com selenium-side-runner + Chromium headless.')
  definition {
    cps {
      sandbox(true)
      script(new File("${pipelinesDir}/functional.Jenkinsfile").text)
    }
  }
}
