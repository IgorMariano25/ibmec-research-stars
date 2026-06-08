// Categoria: TESTE DE DESEMPENHO (Apache JMeter) — IBMEC Research Stars
// Roda o plano de carga contra o backend e publica o relatório HTML + métricas.
pipeline {
  agent { node { label 'built-in' } }
  options {
    timestamps()
    disableConcurrentBuilds()
  }
  environment {
    TARGET_HOST = 'backend'
    TARGET_PORT = '8080'
  }
  stages {
    stage('Aguardar Backend') {
      steps {
        dir('/workspace') {
          sh '''
            set -e
            echo "Aguardando backend em http://$TARGET_HOST:$TARGET_PORT ..."
            for i in $(seq 1 90); do
              http_code=$(curl -s -o /tmp/irs-performance-login-check.out -w "%{http_code}" \
                -X POST "http://$TARGET_HOST:$TARGET_PORT/api/v1/auth/login" \
                -H "Content-Type: application/json" \
                --data '{"email":"admin@ibmec.br","password":"admin123"}' || true)

              if [ "$http_code" = "200" ]; then
                echo "Backend pronto."
                exit 0
              fi

              echo "Backend ainda indisponível (HTTP $http_code). Tentativa $i/90."
              sleep 2
            done

            echo "Backend não ficou pronto para o teste de desempenho."
            cat /tmp/irs-performance-login-check.out 2>/dev/null || true
            exit 1
          '''
        }
      }
    }

    stage('Teste de Desempenho (JMeter)') {
      steps {
        dir('/workspace') {
          sh '''
            set -e
            rm -rf /tmp/irs-ci/jmeter results/jmeter
            mkdir -p /tmp/irs-ci/jmeter results/jmeter
            jmeter -n \
              -t tests/performance/jmeter/research-stars-load-test.jmx \
              -Jhost=$TARGET_HOST -Jport=$TARGET_PORT -Jusers=20 -Jloops=10 \
              -l /tmp/irs-ci/jmeter/results.jtl \
              -e -o /tmp/irs-ci/jmeter/html
            cp -R /tmp/irs-ci/jmeter/. results/jmeter/
          '''
        }
      }
    }
  }
  post {
    always {
      dir('/workspace') {
        perfReport sourceDataFiles: 'results/jmeter/results.jtl',
                   errorFailedThreshold: 5,
                   errorUnstableThreshold: 1
        publishHTML(target: [
          reportDir: 'results/jmeter/html',
          reportFiles: 'index.html',
          reportName: 'Relatorio JMeter',
          keepAll: true,
          alwaysLinkToLastBuild: true,
          allowMissing: true
        ])
        archiveArtifacts artifacts: 'results/jmeter/**', allowEmptyArchive: true
      }
    }
  }
}
