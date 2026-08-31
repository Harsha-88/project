import {
  Args,
  Field,
  InputType,
  Mutation,
  ObjectType,
  Query,
  Resolver,
} from '@nestjs/graphql';
import { IsEmail, IsNotEmpty, MinLength } from 'class-validator';
import { AuthGrpcService } from '../grpc/auth.grpc.service';
import { UserGrpcService } from '../grpc/user.grpc.service';

@InputType()
export class SignupInput {
  @Field()
  @IsEmail()
  email!: string;

  @Field()
  @IsNotEmpty()
  @MinLength(8)
  password!: string;
}

@InputType()
export class LoginInput {
  @Field()
  @IsEmail()
  email!: string;

  @Field()
  @IsNotEmpty()
  @MinLength(8)
  password!: string;
}

@InputType()
export class SessionInput {
  @Field()
  @IsNotEmpty()
  sessionToken!: string;
}

@ObjectType()
export class SignupResponse {
  @Field()
  success!: boolean;

  @Field()
  message!: string;
}

@ObjectType()
export class LoginResponse {
  @Field()
  success!: boolean;

  @Field()
  message!: string;

  @Field()
  sessionToken!: string;
}

@ObjectType()
export class UserProfile {
  @Field()
  id!: string;

  @Field()
  email!: string;

  @Field()
  createdAt!: string;
}

@Resolver()
export class AppResolver {
  constructor(
    private readonly authGrpcService: AuthGrpcService,
    private readonly userGrpcService: UserGrpcService,
  ) {}

  @Query(() => String)
  hello(): string {
    return 'Hello from API Gateway';
  }

  @Query(() => UserProfile)
  async me(
    @Args('input') input: SessionInput,
  ): Promise<UserProfile> {
    const sessionToken = input.sessionToken;
    const email =
      await this.authGrpcService.getSessionEmail(sessionToken);

    return this.userGrpcService.findUserByEmail(email);
  }

  @Mutation(() => SignupResponse)
  async signup(
    @Args('input') input: SignupInput,
  ): Promise<SignupResponse> {
    const user = await this.userGrpcService.createUser(
      input.email,
      input.password,
    );

    return {
      success: true,
      message: `Signup successful for ${user.email}`,
    };
  }

  @Mutation(() => LoginResponse)
  async login(
    @Args('input') input: LoginInput,
  ): Promise<LoginResponse> {
    return this.authGrpcService.login(
      input.email,
      input.password,
    );
  }
}
