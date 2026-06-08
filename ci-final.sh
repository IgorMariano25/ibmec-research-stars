#!/usr/bin/env bash
# Validação final end-to-end com a imagem reconstruída (removido após uso).
set +e

echo "=== Aguardando backend re-seedado ==="
login="000"
for i in $(seq 1 60); do
  login=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://backend:8080/api/v1/auth/login \
    -H "Content-Type: application/json" \
    --data '{"email":"admin@ibmec.br","password":"admin123"}') || true
  if [ "$login" = "200" ]; then echo "backend login: HTTP 200 (pronto em ~${i}s)"; break; fi
  sleep 1
done
[ "$login" != "200" ] && { echo "backend login: HTTP $login (FALHOU)"; }

echo
echo "=== NODE_PATH (deve vir da imagem) e jest-junit ==="
echo "NODE_PATH=$NODE_PATH"
ls -d "$NODE_PATH/jest-junit" 2>/dev/null && echo "jest-junit: OK" || echo "jest-junit: AUSENTE"

echo
echo "=== Reproduzindo job FUNCIONAL (exatamente como o pipeline) ==="
export JEST_JUNIT_OUTPUT_DIR="/tmp/irs-ci/selenium"
export JEST_JUNIT_OUTPUT_NAME="selenium-results.xml"
cd /workspace/tests/functional/selenium
rm -rf /tmp/irs-ci/selenium && mkdir -p /tmp/irs-ci/selenium
selenium-side-runner \
  --output-directory /tmp/irs-ci/selenium \
  --jest-options '"--reporters=default --reporters=jest-junit"' \
  research-stars.side
FUNC_EXIT=$?
echo "FUNCIONAL EXIT_CODE=$FUNC_EXIT"
echo "--- XML JUnit ---"
ls -la /tmp/irs-ci/selenium/*.xml 2>/dev/null && grep -o 'tests="[0-9]*" failures="[0-9]*" errors="[0-9]*"' /tmp/irs-ci/selenium/*.xml | head -n1 || echo "SEM XML"

echo
echo "=== RESUMO ==="
echo "backend login: $login | funcional exit: $FUNC_EXIT"
