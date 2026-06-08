# ============================================================
# Imagem Jenkins customizada — CI de Testes IBMEC Research Stars
# Inclui tudo para rodar os 3 jobs de teste:
#   - JUnit   (Maven + JDK 21)            -> Testes de Unidade
#   - JMeter  (Apache JMeter 5.6.3)       -> Testes de Desempenho
#   - Selenium IDE (selenium-side-runner + Chromium headless) -> Testes Funcionais
# ============================================================
FROM jenkins/jenkins:lts-jdk21

USER root

ENV DEBIAN_FRONTEND=noninteractive \
    JMETER_VERSION=5.6.3 \
    JMETER_HOME=/opt/apache-jmeter \
    CHROME_BIN=/usr/bin/chromium

# ---------- Dependências do sistema ----------
RUN apt-get update && apt-get install -y --no-install-recommends \
        maven \
        curl \
        unzip \
        ca-certificates \
        gnupg \
        chromium \
        chromium-driver \
        fonts-liberation \
        libnss3 \
        libxss1 \
        libgbm1 \
    && rm -rf /var/lib/apt/lists/*

# ---------- Node.js 20 LTS + selenium-side-runner ----------
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y --no-install-recommends nodejs \
    && npm install -g selenium-side-runner@4 \
    && rm -rf /var/lib/apt/lists/*

# ---------- Apache JMeter ----------
# Tenta o CDN principal (dlcdn) e cai para o archive; com retentativas para
# tolerar falhas de rede transitórias (curl exit 7 = falha de conexão).
RUN set -eux; \
    for url in \
        "https://dlcdn.apache.org/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz" \
        "https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz"; do \
        if curl -fSL --retry 5 --retry-delay 5 --retry-connrefused "$url" -o /tmp/jmeter.tgz; then \
            break; \
        fi; \
    done; \
    test -s /tmp/jmeter.tgz; \
    tar -xzf /tmp/jmeter.tgz -C /opt; \
    ln -s "/opt/apache-jmeter-${JMETER_VERSION}" "${JMETER_HOME}"; \
    ln -s "${JMETER_HOME}/bin/jmeter" /usr/local/bin/jmeter; \
    rm -f /tmp/jmeter.tgz

# chromedriver instalado pelo pacote chromium-driver fica em /usr/bin/chromedriver
ENV PATH="${JMETER_HOME}/bin:${PATH}"

# O customWorkspace dos jobs usa /workspace. O Jenkins cria diretórios temporários
# irmãos como /workspace@tmp para executar steps sh; deixe-os graváveis.
RUN mkdir -p /workspace /workspace@tmp \
    && chown -R jenkins:jenkins /workspace /workspace@tmp

# ---------- Configuration as Code + jobs ----------
ENV CASC_JENKINS_CONFIG=/var/jenkins_conf/casc.yaml \
    JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

COPY casc.yaml /var/jenkins_conf/casc.yaml
COPY jobs/seed.groovy /var/jenkins_conf/seed.groovy
COPY pipelines/ /var/jenkins_conf/pipelines/
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt

# ---------- Plugins ----------
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# ---------- Reporter JUnit para os testes funcionais ----------
# jest-junit gera o XML consumido pelo passo 'junit' do Jenkins. NODE_PATH
# permite ao Jest (embutido no selenium-side-runner) resolver o reporter global.
RUN npm install -g jest-junit
ENV NODE_PATH=/usr/lib/node_modules

USER jenkins
