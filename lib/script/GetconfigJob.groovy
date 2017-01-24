node {

  stage 'Git�N���[��'


    // We can just run it with "externalCall(...)" since it has a call method.

  git "${GetConfigSCM}"
  bat 'git branch --set-upstream-to=origin/master master'
  bat 'git pull'


  echo "�����V�i���I��ݒ肵�܂�..."
  def externalMethod = load("lib/script/externalMethod.groovy")
  def branches = externalMethod.getBranches()
  def config_files = externalMethod.getConfigFiles()
  def v = input message: '�ǂ̊��̌����V�i���I�����s���܂���?',
      ok: '��������',
      parameters: [
        [$class: 'ChoiceParameterDefinition',
            choices: branches.join("\n"),
            description: 'Git�u�����`',
            name: 'targetBranch'],
        [$class: 'ChoiceParameterDefinition',
            choices: config_files.join("\n"),
            description: '�R���t�B�O�t�@�C��',
            name: '-c'],
        [$class: 'StringParameterDefinition',
            description: '�����Ώۂ̍i�荞��(-s) ���I�v�V����',
            name: '-s'],
        [$class: 'StringParameterDefinition',
            description: '����ID�̍i�荞��(-t) ���I�v�V����',
            name: '-t']
      ]
  if (v == null) {
     error '�I�����Ă�������'
  }

  stage '�����V�i���I���s'

  echo "${v['targetBranch']}�ɃX�C�b�`���܂�..."
  bat "git checkout ${v['targetBranch']}"

  echo "getconfig �����s���܂�..."
  env.PATH = "c:\\server-acceptance;${env.PATH}"
  def getconfig_opt = "-c config/${v['-c']}"
  if (v['-s']) {
    getconfig_opt += ' -s ' + v['-s']
  }
  if (v['-t']) {
    getconfig_opt += ' -t ' + v['-t']
  }
  bat "getconfig ${getconfig_opt}"

  stage 'Json�o�^'

  echo "getconfig ���s���ʂ�o�^���܂�..."
  bat "getconfig -u local"

  stage 'Git�R�~�b�g'

  current_env = env.getEnvironment()
  def comment = "Jenkins job=${current_env['JOB_NAME']}[${current_env['BUILD_ID']}]"
  println current_env
  bat 'git add .'
  bat "git commit -a -m \"${comment}\""
  // bat 'git push --set-upstream origin master'
  bat 'git push'
}

def getBranches() {
  def branch_output = bat script : 'git branch -a --sort=-committerdate',
                      returnStdout : true

  def branches = []
  def branch_lines = branch_output.split("\n")
  for (ii = 0; ii < branch_lines.size() && ii < 10; ii++) {
    def line = branch_lines[ii]
    def matcher = line =~ 'remotes/origin/(.+)'
    if (matcher) {
      branches.add(matcher[0][1])
    }
  }
  if (branches.size() == 0) {
     error "�u�����`���擾�ł��܂��� : ${branch_output}"
  }
  return branches
}

@NonCPS
def getConfigFiles() {
  def workspace = pwd()
  def config_files = []
  new File("${workspace}/config").eachFile { config_file ->
    def matcher = config_file.name =~ '(config.*\\.groovy)'
    if (matcher) {
      config_files.add(matcher[0][0])
    }
  }
  if (config_files.size() == 0) {
     error "config �t�@�C�����擾�ł��܂��� : ${branch_output}"
  }
  return config_files
}
