import { Injectable } from '@nestjs/common';
import { RpcException } from '@nestjs/microservices';
import { status } from '@grpc/grpc-js';
import { PrismaService } from '../prisma/prisma.service';
import * as argon2 from 'argon2';

export interface UserResponse {
  id: string;
  email: string;
  password: string;
  createdAt: string;
}

@Injectable()
export class UserService {
  constructor(private readonly prisma: PrismaService) {}

  async createUser(email: string, password: string): Promise<UserResponse> {
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

    const user = await this.prisma.user.create({
      data: {
        email,
        password: hashedPassword,
      },
    });

    return {
      id: user.id,
      email: user.email,
      password: user.password,
      createdAt: user.createdAt.toISOString(),
    };
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
      password: user.password,
      createdAt: user.createdAt.toISOString(),
    };
  }
}
