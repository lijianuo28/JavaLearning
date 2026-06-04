# 斐波那契数列第 n 项计算
# 输入：从内存地址 0x0 读取 n（通过 WD 命令预先写入）
# 输出：结果存入 R3，并写入内存地址 0x4，最后触发 ebreak

    .text
    .globl main

main:
    # 从内存地址 0x0 读取 n 到 R2
    li      t0, 0x0
    lw      x2, 0(t0)              # R2 = n

    # 将 n 复制到 t0 备用
    addi    t0, x2, 0

    # 处理 n <= 0 的情况（可选，根据需求）
    li      t1, 1
    blt     t0, t1, case_0         # 若 n < 1，结果置 0

    # 处理 n = 1
    beq     t0, t1, case0

    # 处理 n = 2
    li      t1, 2
    beq     t0, t1, case1

    # n >= 3 初始化
    li      t2, 1                  # t2 = F(1)
    li      t3, 1                  # t3 = F(2)
    li      t4, 3                  # t4 = i = 3

loop:
    add     t5, t2, t3             # t5 = F(i)
    mv      t2, t3                 # 更新 F(i-2)
    mv      t3, t5                 # 更新 F(i-1)
    addi    t4, t4, 1              # i++
    blt     t4, t0, loop           # 若 i < n 继续
    beq     t4, t0, loop           # 若 i == n 继续（最后一次）

    # 循环结束，结果在 t3 中，存入 R3
    mv      x3, t3
    j       output

case0:
    li      x3, 1                  # F(1) = 1
    j       output

case1:
    li      x3, 1                  # F(2) = 1
    j       output

case_0:
    li      x3, 0                  # 未定义输入，返回 0
    j       output

output:
    # 将结果写入内存地址 0x4（便于用 RD 命令查看）
    li      t0, 0x4
    sw      x3, 0(t0)

    # 触发 PDU 框架结束（兼容两种方式）
    ebreak