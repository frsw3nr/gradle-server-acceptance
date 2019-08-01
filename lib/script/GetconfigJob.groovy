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
        [$class: 'PasswordParameterDefinition',
            description: '�p�X���[�h(-k) ���Í������ꂽ�R���t�B�O�t�@�C��(-encrypted)�̏ꍇ�͕K�{',
            name: '-k'],
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
  if (v['-k']) {
    getconfig_opt += ' -k ' + v['-k']
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
  bat 'git config --global user.email "jenkins@example.com"'
  bat 'git config --global user.name "Jenkins"'
  bat "git commit -a -m \"${comment}\""
  // bat 'git push --set-upstream origin master'
  bat 'git push'
}
