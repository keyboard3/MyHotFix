package com.keyboard3.buildsrc

import org.objectweb.asm.*

/**
 * @author keyboard3 on 2018/1/5
 */

public class InjectClassVisitor extends ClassVisitor {
    public InjectClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                     String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        mv = new MethodVisitor(Opcodes.ASM4, mv) {
            @Override
            void visitInsn(int opcode) {
                if ("<init>".equals(name) && opcode == Opcodes.RETURN) {
                    Label l1 = new Label();
                    super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
                    super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                    super.visitJumpInsn(Opcodes.IFEQ, l1);
                    super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                    super.visitLdcInsn(Type.getType("Lcn/jiajixin/nuwa/Hack;"));
                    super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
                    super.visitLabel(l1);
                }
                super.visitInsn(opcode);
            }

            @Override
            public void visitMaxs(int maxStack, int maxLocal) {
                if ("<init>".equals(name)) {
                    super.visitMaxs(maxStack + 2, maxLocal);
                } else {
                    super.visitMaxs(maxStack, maxLocal);
                }
            }
        };
        return mv;
    }
}
