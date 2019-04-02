import time

def two():
  x = 0
  for i in range(1, 10):
    x -= i

  #time.sleep(2)
  return x


def one():
  x = 0
  for i in range(1, 9999999999999999):
    x += i

  return x


def main():
  x = 0

  while True:
    x += 0.001

    x += one()

    x += two()




if __name__ == '__main__':
  main()

