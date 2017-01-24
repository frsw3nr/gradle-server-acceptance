node {
  stage 'Git�N���[��'
  // git 'https://github.com/frsw3nr/gradle-server-acceptance.git'
  // git ''http://testgit001/gitbucket/git/server-acceptance/gradle-server-acceptance.git'
  git "${GetConfigBaseSCM}"

  echo "�f�v���C�V�i���I��ݒ肵�܂�..."
  def branches = getBranches()

  def v = input message: '�ǂ̊��̃f�v���C�V�i���I�����s���܂���?',
      ok: '�f�v���C����',
      parameters: [
        [$class: 'ChoiceParameterDefinition',
            choices: branches.join("\n"),
            description: 'Git�u�����`�̎w��',
            name: 'targetBranch'],
        [$class: 'BooleanParameterDefinition',
            defaultValue: false,
            description: '���j�b�g�e�X�g�̎��s�L��',
            name: 'testOption'],
        [$class: 'StringParameterDefinition',
            description: '���W���[���z�z��',
            defaultValue: 'c:\\',
            name: 'targetDirectory'],
      ]
  if (v == null) {
     error '�I�����Ă�������'
  }

  echo "${v['targetBranch']}�ɃX�C�b�`���܂�..."
  bat "git checkout ${v['targetBranch']}"

  stage '�P�̃e�X�g'
  bat 'gradle clean'
  if (v['testOption']) {
    bat 'gradle test'
  } else {
    echo '�X�L�b�v'
  }

  stage '�A�[�J�C�u'
  bat 'gradle shadowJar'
  bat 'gradle zipApp'

  env.TARGET_DIR = v['targetDirectory']
  bat '''\
    |cd "%TARGET_DIR%"
    |"C:\\Program Files\\7-Zip\\7z.exe" x "%WORKSPACE%\\build\\distributions\\gradle-server-acceptance-*.zip" -y
    |'''.stripMargin()

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
