import {
  Inject,
  Injectable,
  OnModuleInit,
} from '@nestjs/common';
import type { ClientGrpc } from '@nestjs/microservices';
import { firstValueFrom, Observable } from 'rxjs';

interface CreateUserRequest {
  email: string;
  password: string;
}

interface FindUserByEmailRequest {
  email: string;
}

export interface UserProfile {
  id: string;
  email: string;
  createdAt: string;
}

interface UserServiceClient {
  createUser(
    data: CreateUserRequest,
  ): Observable<UserProfile>;

  findUserByEmail(
    data: FindUserByEmailRequest,
  ): Observable<UserProfile>;
}

@Injectable()
export class UserGrpcService implements OnModuleInit {
  private userService!: UserServiceClient;

  constructor(
    @Inject('USER_GRPC')
    private readonly client: ClientGrpc,
  ) {}

  onModuleInit(): void {
    this.userService =
      this.client.getService<UserServiceClient>('UserService');
  }

  async createUser(
    email: string,
    password: string,
  ): Promise<UserProfile> {
    const user = await firstValueFrom(
      this.userService.createUser({ email, password }),
    );

    return {
      id: user.id,
      email: user.email,
      createdAt: user.createdAt,
    };
  }

  async findUserByEmail(email: string): Promise<UserProfile> {
    const user = await firstValueFrom(
      this.userService.findUserByEmail({ email }),
    );

    return {
      id: user.id,
      email: user.email,
      createdAt: user.createdAt,
    };
  }
}
