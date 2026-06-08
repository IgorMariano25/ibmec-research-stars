// Categoria: TESTE DE UNIDADE (JUnit) — IBMEC Research Stars
// Executa os testes unitários taggeados com @Tag("unit") via Maven Surefire.
pipeline {
  agent { node { label 'built-in' } }
  options {
    timestamps()
    disableConcurrentBuilds()
  }
  stages {
    stage('Testes de Unidade (JUnit)') {
      steps {
        dir('/workspace') {
          sh '''
            set -e
            rm -rf results/unit
            mkdir -p results/unit
            mvn -B -ntp -Dtest.groups=unit test
            cp -R target/surefire-reports results/unit/
          '''
        }
      }
    }
  }
  post {
    always {
      dir('/workspace') {
        junit testResults: 'results/unit/surefire-reports/*.xml', allowEmptyResults: false
        archiveArtifacts artifacts: 'results/unit/surefire-reports/**', allowEmptyArchive: true
      }
    }
  }
}
