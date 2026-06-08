// SUITE COMPLETA — dispara as 3 categorias de teste em sequência.
pipeline {
  agent { node { label 'built-in' } }
  options { timestamps() }
  stages {
    stage('01 • Testes de Unidade') {
      steps { build job: 'research-stars-testes/01-testes-de-unidade', wait: true }
    }
    stage('02 • Testes de Desempenho') {
      steps { build job: 'research-stars-testes/02-testes-de-desempenho', wait: true }
    }
    stage('03 • Testes Funcionais') {
      steps { build job: 'research-stars-testes/03-testes-funcionais', wait: true }
    }
  }
}
