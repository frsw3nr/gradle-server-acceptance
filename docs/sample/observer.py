class Employee:
  def __init__(self, name, title, salary, payroll):
    self.name = name
    self.title = title
    self.salary = salary
    self.payroll = payroll

  def update_salary(self, new_salary):
    self.salary = new_salary
    self.payroll.update(self)

class Employee2:
  def __init__(self, name, title, salary ):
    self.name = name
    self.title = title
    self.salary = salary
    self.observers = []

  def add_observer(self, observer):
    self.observers.append(observer) 

  def update_salary(self, new_salary):
    self.salary = new_salary
    self.notify_observers()

  def notify_observers(self):
    for  observer  in  self.observers :
      observer.update(self)

class Payroll:
  def __init__(self):
    pass
  def update(self, changed_employee):  # Subjectオブジェクトを受け取る
    print ( changed_employee.name + "の給料が" + str(changed_employee.salary) + "ドルに上がりました!" )    
    print ( "経理部門は" + changed_employee.name + "に小切手を切ります！")  

class Taxman:
  def __init__(self):
    pass
  def update(self, changed_employee):  # Subjectオブジェクトを受け取る
    print ( changed_employee.name + "の給料が" + str(changed_employee.salary) + "ドルに上がりました!" )    
    print ( "税務署員は" + changed_employee.name + "に新しい税金請求書を送ります！") 


# payroll = Payroll()
# employee = Employee( "tsuji","leader", 1000, payroll  )
# employee.update_salary(2000)

taxman = Taxman()
payroll = Payroll()
employee2 = Employee2("daisuke","member", 3000)

employee2.add_observer(payroll)
employee2.add_observer(taxman)

employee2.update_salary(4000)

