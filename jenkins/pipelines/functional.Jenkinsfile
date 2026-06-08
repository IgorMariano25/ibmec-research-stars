// Categoria: TESTES FUNCIONAIS (Selenium IDE) — IBMEC Research Stars
// Executa o projeto .side com selenium-side-runner + Chromium headless contra o frontend.
pipeline {
  agent { node { label 'built-in' } }
  options {
    timestamps()
    disableConcurrentBuilds()
  }
  environment {
    // As capabilities do navegador (binário do Chromium, flags --no-sandbox etc.)
    // ficam em tests/functional/selenium/.side.yml (lido automaticamente pelo runner v4).
    FRONTEND_URL = 'http://frontend'
    NODE_PATH = '/usr/lib/node_modules'
    JEST_JUNIT_OUTPUT_DIR = '/tmp/irs-ci/selenium'
    JEST_JUNIT_OUTPUT_NAME = 'selenium-results.xml'
  }
  stages {
    stage('Aguardar Aplicação') {
      steps {
        dir('/workspace') {
          sh '''
            set -e
            echo "Aguardando frontend e backend via $FRONTEND_URL ..."
            for i in $(seq 1 90); do
              frontend_code=$(curl -s -o /dev/null -w "%{http_code}" "$FRONTEND_URL/login" || true)
              login_code=$(curl -s -o /tmp/irs-functional-login-check.out -w "%{http_code}" \
                -X POST "$FRONTEND_URL/api/v1/auth/login" \
                -H "Content-Type: application/json" \
                --data '{"email":"admin@ibmec.br","password":"admin123"}' || true)

              if [ "$frontend_code" = "200" ] && [ "$login_code" = "200" ]; then
                echo "Aplicação pronta."
                exit 0
              fi

              echo "Aplicação ainda indisponível (frontend HTTP $frontend_code, login HTTP $login_code). Tentativa $i/90."
              sleep 2
            done

            echo "Aplicação não ficou pronta para o teste funcional."
            cat /tmp/irs-functional-login-check.out 2>/dev/null || true
            exit 1
          '''
        }
      }
    }

    stage('Testes Funcionais (Selenium IDE)') {
      steps {
        dir('/workspace/tests/functional/selenium') {
          sh '''
            set -e
            rm -rf /tmp/irs-ci/selenium /workspace/results/selenium
            mkdir -p /tmp/irs-ci/selenium /workspace/results/selenium
            selenium-side-runner \
              --output-directory /tmp/irs-ci/selenium \
              --jest-options '"--reporters=default --reporters=jest-junit"' \
              research-stars.side
            cp -R /tmp/irs-ci/selenium/. /workspace/results/selenium/
          '''
        }
      }
    }
  }
  post {
    always {
      dir('/workspace') {
        junit testResults: 'results/selenium/*.xml', allowEmptyResults: false
        archiveArtifacts artifacts: 'results/selenium/**', allowEmptyArchive: true
      }
    }
  }
}
