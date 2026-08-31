import { Injectable } from '@nestjs/common';
import { RpcException } from '@nestjs/microservices';
import { status } from '@grpc/grpc-js';
import * as argon2 from 'argon2';
import { PrismaService } from '../prisma/prisma.service';

export interface UserResponse {
  id: string;
  email: string;
  createdAt: string;
}

@Injectable()
export class UserService {
  constructor(private readonly prisma: PrismaService) {}

  async createUser(
    email: string,
    password: string,
  ): Promise<UserResponse> {
    const existingUser = await this.prisma.user.findUnique({
      where: { email },
    });

    if (existingUser) {
      throw new RpcException({
        code: status.ALREADY_EXISTS,
        message: 'User already exists',
      });
    }

    const hashedPassword = await argon2.hash(password, {
      type: argon2.argon2id,
    });

    try {
      const result = await this.prisma.$transaction(async (tx) => {
        const user = await tx.user.create({
          data: {
            email,
            password: hashedPassword,
          },
        });

        await tx.outboxEvent.create({
          data: {
            eventType: 'USER_CREATED',
            payload: {
              userId: user.id,
              email: user.email,
              timestamp: new Date().toISOString(),
            },
          },
        });

        return user;
      });

      return {
        id: result.id,
        email: result.email,
        createdAt: result.createdAt.toISOString(),
      };
    } catch (error: unknown) {
      if (
        typeof error === 'object' &&
        error !== null &&
        'code' in error &&
        error.code === 'P2002'
      ) {
        throw new RpcException({
          code: status.ALREADY_EXISTS,
          message: 'User already exists',
        });
      }

      throw error;
    }
  }

  async findUserByEmail(email: string): Promise<UserResponse> {
    const user = await this.prisma.user.findUnique({
      where: { email },
    });

    if (!user) {
      throw new RpcException({
        code: status.NOT_FOUND,
        message: 'User not found',
      });
    }

    return {
      id: user.id,
      email: user.email,
      createdAt: user.createdAt.toISOString(),
    };
  }
}
