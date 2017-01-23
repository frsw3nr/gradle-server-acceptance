node {
  stage 'Git�N���[��'
  git "${GetConfigScenarioSCM}"
  def branches = getBranches()

  def v = input message: '�ǂ̊��̃f�v���C�V�i���I�����s���܂���?',
      ok: '�f�v���C����',
      parameters: [
        [$class: 'ChoiceParameterDefinition',
            choices: branches.join("\n"),
            description: 'Git�u�����`�̎w��',
            name: 'targetBranch'],
        [$class: 'StringParameterDefinition',
            description: '���W���[���z�z��',
            defaultValue: 'c:\\getconfig',
            name: 'targetDirectory'],
      ]
  if (v == null) {
     error '�I�����Ă�������'
  }

  stage '�A�[�J�C�u'
println v
  env.TARGET_DIR = v['targetDirectory']?:v
  bat '''\
    |getconfig -x "%TARGET_DIR%\\%JOB_BASE_NAME%.zip"
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
